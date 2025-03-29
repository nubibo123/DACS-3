package com.example.dacs3.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dacs3.data.User
import com.example.dacs3.data.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthEvent {
    object NavigateToLogin : AuthEvent()
    data class ShowError(val message: String) : AuthEvent()
}

class AuthViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private val _authEvent = MutableStateFlow<AuthEvent?>(null)
    val authEvent: StateFlow<AuthEvent?> = _authEvent

    init {
        viewModelScope.launch {
            // Check for saved user session
            userPreferences.getUserFlow().collect { user ->
                user?.let {
                    _authState.value = AuthState.Success(it)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                // Clear any previous error events
                _authEvent.value = null
                
                if (email.isBlank()) {
                    _authEvent.value = AuthEvent.ShowError("Email cannot be empty")
                    return@launch
                }
                if (password.isBlank()) {
                    _authEvent.value = AuthEvent.ShowError("Password cannot be empty")
                    return@launch
                }
                
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    val user = User(
                        email = firebaseUser.email ?: "",
                        username = firebaseUser.displayName ?: "",
                        password = "", // We don't store passwords with Firebase Auth
                        fullName = firebaseUser.displayName ?: "",
                        profilePicture = firebaseUser.photoUrl?.toString() ?: ""
                    )
                    handleLoginSuccess(user)
                } ?: run {
                    _authEvent.value = AuthEvent.ShowError("Login failed. Please check your credentials.")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Invalid email or password"
                    e.message?.contains("INVALID_EMAIL") == true -> "Invalid email format"
                    e.message?.contains("TOO_MANY_ATTEMPTS_TRY_LATER") == true -> "Too many attempts. Please try again later"
                    e.message?.contains("USER_DISABLED") == true -> "This account has been disabled"
                    e.message?.contains("NETWORK") == true -> "Network error. Please check your connection"
                    else -> "An error occurred. Please try again"
                }
                _authEvent.value = AuthEvent.ShowError(errorMessage)
            }
        }
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                // Clear any previous error events
                _authEvent.value = null
                
                if (username.isBlank()) {
                    _authEvent.value = AuthEvent.ShowError("Username cannot be empty")
                    return@launch
                }
                if (email.isBlank()) {
                    _authEvent.value = AuthEvent.ShowError("Email cannot be empty")
                    return@launch
                }
                if (password.isBlank()) {
                    _authEvent.value = AuthEvent.ShowError("Password cannot be empty")
                    return@launch
                }
                if (password.length < 6) {
                    _authEvent.value = AuthEvent.ShowError("Password must be at least 6 characters long")
                    return@launch
                }

                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let { firebaseUser ->
                    // Update display name in Firebase
                    firebaseUser.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                    ).await()
                    
                    val user = User(
                        email = email,
                        username = username,
                        password = "", // We don't store passwords with Firebase Auth
                        fullName = username
                    )
                    userPreferences.saveUser(user)
                    _authState.value = AuthState.Success(user)
                } ?: run {
                    _authEvent.value = AuthEvent.ShowError("Failed to create user. Please try again")
                }
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("EMAIL_EXISTS") == true || e.message?.contains("The email address is already in use by another account.") == true -> "Email is already registered"
                    e.message?.contains("INVALID_EMAIL") == true -> "Invalid email format"
                    e.message?.contains("WEAK_PASSWORD") == true -> "Password is too weak"
                    e.message?.contains("NETWORK") == true -> "Network error. Please check your connection"
                    e.message?.contains("username-exists") == true -> "Username is already taken"
                    else -> "An error occurred during sign up. Please try again"
                }
                _authEvent.value = AuthEvent.ShowError(errorMessage)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
                userPreferences.clearUser()
                _authState.value = AuthState.Initial
                _authEvent.value = AuthEvent.NavigateToLogin
            } catch (e: Exception) {
                _authEvent.value = AuthEvent.ShowError("Failed to logout: ${e.message}")
            }
        }
    }

    fun clearEvents() {
        _authEvent.value = null
    }

    private suspend fun handleLoginSuccess(user: User) {
        try {
            userPreferences.saveUser(user)
            _authState.value = AuthState.Success(user)
        } catch (e: Exception) {
            _authEvent.value = AuthEvent.ShowError("Failed to save user session: ${e.message}")
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
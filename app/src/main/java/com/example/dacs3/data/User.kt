package com.example.dacs3.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val uid: String = "",
    val username: String,
    val email: String,
    val password: String = "", // Only used temporarily during registration
    val fullName: String = "",
    val profilePicture: String = ""
)
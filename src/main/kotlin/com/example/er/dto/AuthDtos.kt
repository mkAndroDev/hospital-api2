package com.example.er.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val username: String,
    val fullName: String,
    val role: String,
    val expiresIn: Long // seconds
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val fullName: String,
    val role: String
)

@Serializable
data class UserResponse(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    val createdAt: String,
    val isActive: Boolean
)
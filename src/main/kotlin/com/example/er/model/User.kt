package com.example.er.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : LongIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val fullName = varchar("full_name", 100)
    val role = varchar("role", 20)
    val createdAt = datetime("created_at")
    val isActive = bool("is_active")
}

data class User(
    val id: Long,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: UserRole,
    val createdAt: LocalDateTime,
    val isActive: Boolean
)

enum class UserRole {
    ADMIN,      // Full access
    DOCTOR,     // Can admit and handle patients
    NURSE       // Can view and handle patients
}
package com.example.er.repository

import com.example.er.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class UserRepository {

    fun create(
        username: String,
        passwordHash: String,
        fullName: String,
        role: UserRole,
        createdAt: LocalDateTime = LocalDateTime.now()
    ): User = transaction {
        val id = Users.insertAndGetId {
            it[Users.username] = username
            it[Users.passwordHash] = passwordHash
            it[Users.fullName] = fullName
            it[Users.role] = role.name
            it[Users.createdAt] = createdAt
            it[Users.isActive] = true
        }

        User(
            id = id.value,
            username = username,
            passwordHash = passwordHash,
            fullName = fullName,
            role = role,
            createdAt = createdAt,
            isActive = true
        )
    }

    fun findById(id: Long): User? = transaction {
        Users.select { Users.id eq id }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findByUsername(username: String): User? = transaction {
        Users.select { Users.username eq username }
            .map { it.toUser() }
            .singleOrNull()
    }

    fun findAll(): List<User> = transaction {
        Users.selectAll()
            .map { it.toUser() }
    }

    fun usernameExists(username: String): Boolean = transaction {
        Users.select { Users.username eq username }.count() > 0
    }

    fun deactivateUser(id: Long): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[isActive] = false
        } > 0
    }

    private fun ResultRow.toUser() = User(
        id = this[Users.id].value,
        username = this[Users.username],
        passwordHash = this[Users.passwordHash],
        fullName = this[Users.fullName],
        role = UserRole.valueOf(this[Users.role]),
        createdAt = this[Users.createdAt],
        isActive = this[Users.isActive]
    )
}
package com.example.er.service

import com.example.er.dto.*
import com.example.er.model.User
import com.example.er.model.UserRole
import com.example.er.repository.UserRepository
import com.example.er.security.JwtConfig
import com.example.er.security.PasswordHasher
import org.slf4j.LoggerFactory

class AuthService(private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    fun login(request: LoginRequest): Result<LoginResponse> {
        logger.info("Login attempt for username: ${request.username}")

        val user = userRepository.findByUsername(request.username)

        if (user == null) {
            logger.warn("User not found: ${request.username}")
            return Result.failure(AuthException("Invalid username or password"))
        }

        logger.info("User found: ${user.username}, role: ${user.role}, active: ${user.isActive}")

        if (!user.isActive) {
            logger.warn("User account deactivated: ${request.username}")
            return Result.failure(AuthException("Account is deactivated"))
        }

        logger.info("Verifying password...")
        val passwordMatch = PasswordHasher.verifyPassword(request.password, user.passwordHash)
        logger.info("Password verification result: $passwordMatch")

        if (!passwordMatch) {
            logger.warn("Invalid password for user: ${request.username}")
            return Result.failure(AuthException("Invalid username or password"))
        }

        val token = JwtConfig.generateToken(user.id, user.username, user.role.name)
        logger.info("Login successful for user: ${request.username}")

        return Result.success(
            LoginResponse(
                token = token,
                username = user.username,
                fullName = user.fullName,
                role = user.role.name,
                expiresIn = JwtConfig.getTokenValidityInSeconds()
            )
        )
    }

    fun register(request: RegisterRequest, requesterRole: UserRole?): Result<User> {
        // Only ADMIN can register new users
        if (requesterRole != UserRole.ADMIN) {
            return Result.failure(AuthException("Only administrators can register new users"))
        }

        if (userRepository.usernameExists(request.username)) {
            return Result.failure(AuthException("Username already exists"))
        }

        if (request.password.length < 6) {
            return Result.failure(ValidationException("Password must be at least 6 characters"))
        }

        val role = try {
            UserRole.valueOf(request.role.uppercase())
        } catch (e: Exception) {
            return Result.failure(ValidationException("Invalid role. Must be ADMIN, DOCTOR, or NURSE"))
        }

        val passwordHash = PasswordHasher.hashPassword(request.password)

        val user = userRepository.create(
            username = request.username,
            passwordHash = passwordHash,
            fullName = request.fullName,
            role = role
        )

        return Result.success(user)
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }
}

class AuthException(message: String) : Exception(message)
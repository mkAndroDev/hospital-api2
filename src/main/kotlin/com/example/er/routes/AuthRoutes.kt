package com.example.er.routes

import com.example.er.dto.*
import com.example.er.model.UserRole
import com.example.er.service.AuthException
import com.example.er.service.AuthService
import com.example.er.service.ValidationException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

fun Application.configureAuthRoutes() {
    val authService by inject<AuthService>()
    val logger = LoggerFactory.getLogger("AuthRoutes")

    routing {
        route("/auth") {

            // Public endpoint - login
            post("/login") {
                try {
                    logger.info("=== LOGIN REQUEST ===")
                    logger.info("Headers: ${call.request.headers.entries()}")
                    logger.info("Content-Type: ${call.request.contentType()}")

                    val request = call.receive<LoginRequest>()
                    logger.info("Username: ${request.username}")
                    logger.info("Password length: ${request.password.length}")

                    authService.login(request)
                        .onSuccess { response ->
                            logger.info("Login successful for user: ${request.username}")
                            call.respond(HttpStatusCode.OK, response)
                        }
                        .onFailure { exception ->
                            logger.error("Login failed for user: ${request.username}", exception)
                            when (exception) {
                                is AuthException -> {
                                    call.respond(
                                        HttpStatusCode.Unauthorized,
                                        ErrorResponse(exception.message ?: "Authentication failed", "AUTH_FAILED")
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse("Internal server error", "INTERNAL_ERROR")
                                    )
                                }
                            }
                        }
                } catch (e: Exception) {
                    logger.error("Error processing login request", e)
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid request format: ${e.message}", "INVALID_REQUEST")
                    )
                }
            }

            // Protected endpoints - require authentication
            authenticate("auth-jwt") {

                // Register new user (ADMIN only)
                post("/register") {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()?.let {
                        try { UserRole.valueOf(it) } catch (e: Exception) { null }
                    }

                    val request = call.receive<RegisterRequest>()

                    authService.register(request, role)
                        .onSuccess { user ->
                            call.respond(
                                HttpStatusCode.Created,
                                UserResponse(
                                    id = user.id,
                                    username = user.username,
                                    fullName = user.fullName,
                                    role = user.role.name,
                                    createdAt = user.createdAt.toString(),
                                    isActive = user.isActive
                                )
                            )
                        }
                        .onFailure { exception ->
                            when (exception) {
                                is AuthException -> {
                                    call.respond(
                                        HttpStatusCode.Forbidden,
                                        ErrorResponse(exception.message ?: "Access denied", "FORBIDDEN")
                                    )
                                }
                                is ValidationException -> {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Validation error", "VALIDATION_ERROR")
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse("Internal server error", "INTERNAL_ERROR")
                                    )
                                }
                            }
                        }
                }

                // Get current user info
                get("/me") {
                    val principal = call.principal<JWTPrincipal>()
                    val username = principal?.payload?.subject
                    val role = principal?.payload?.getClaim("role")?.asString()
                    val userId = principal?.payload?.getClaim("userId")?.asLong()

                    call.respond(
                        HttpStatusCode.OK,
                        mapOf(
                            "userId" to userId,
                            "username" to username,
                            "role" to role
                        )
                    )
                }

                // Get all users (ADMIN only)
                get("/users") {
                    val principal = call.principal<JWTPrincipal>()
                    val role = principal?.payload?.getClaim("role")?.asString()

                    if (role != UserRole.ADMIN.name) {
                        call.respond(
                            HttpStatusCode.Forbidden,
                            ErrorResponse("Access denied. Admin role required.", "FORBIDDEN")
                        )
                        return@get
                    }

                    val users = authService.getAllUsers()
                    call.respond(
                        HttpStatusCode.OK,
                        users.map { user ->
                            UserResponse(
                                id = user.id,
                                username = user.username,
                                fullName = user.fullName,
                                role = user.role.name,
                                createdAt = user.createdAt.toString(),
                                isActive = user.isActive
                            )
                        }
                    )
                }
            }
        }
    }
}
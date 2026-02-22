package com.example.er.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    // In production, use environment variables!
    private const val SECRET = "your-secret-key-change-this-in-production-use-env-variable"
    private const val ISSUER = "er-service"
    private const val AUDIENCE = "er-service-users"
    private const val VALIDITY_MS = 3600000L // 1 hour

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .withAudience(AUDIENCE)
        .build()

    fun generateToken(userId: Long, username: String, role: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withSubject(username)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }

    fun getTokenValidityInSeconds(): Long = VALIDITY_MS / 1000
}
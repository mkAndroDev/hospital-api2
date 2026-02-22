package com.example.er

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.er.di.appModule
import com.example.er.routes.configureAuthRoutes
import com.example.er.routes.configurePatientRoutes
import com.example.er.security.JwtConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val logger = LoggerFactory.getLogger("Application")

    // Database configuration
    val dbUrl = System.getenv("DB_URL")
        ?: runCatching { environment.config.property("database.url").getString() }.getOrNull()
        ?: "jdbc:postgresql://localhost:5432/er_db"

    val dbUser = System.getenv("DB_USER")
        ?: runCatching { environment.config.property("database.user").getString() }.getOrNull()
        ?: "er_user"

    val dbPassword = System.getenv("DB_PASSWORD")
        ?: runCatching { environment.config.property("database.password").getString() }.getOrNull()
        ?: "er_password"

    logger.info("Connecting to database: $dbUrl")

    // Configure HikariCP
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = dbUser
        password = dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    val dataSource = HikariDataSource(hikariConfig)

    // Run Flyway migrations
    logger.info("Running database migrations...")
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
    flyway.migrate()
    logger.info("Database migrations completed")

    // Connect Exposed
    Database.connect(dataSource)

    // Install Koin
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    // Install ContentNegotiation
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // Install JWT Authentication
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)

            validate { credential ->
                if (credential.payload.audience.contains("er-service-users")) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("message" to "Invalid or expired token", "code" to "UNAUTHORIZED")
                )
            }
        }
    }

    // Install StatusPages for error handling
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("message" to "Internal server error", "code" to "INTERNAL_ERROR")
            )
        }
    }

    // Configure routes
    configureAuthRoutes()
    configurePatientRoutes()

    logger.info("Emergency Room Service started successfully with JWT authentication")
}
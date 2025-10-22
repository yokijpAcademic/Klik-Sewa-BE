package com.gity.config

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val email: EmailConfig,
    val jwt: JwtConfig,
    val app: ApplicationConfig
)

data class DatabaseConfig(
    val uri: String
)

data class RedisConfig(
    val host: String,
    val port: Int,
    val password: String?
)

data class EmailConfig(
    val apiKey: String,
    val senderEmail: String,
    val senderName: String
)

data class JwtConfig(
    val secret: String,
    val expirationInMinutes: Long
)

data class ApplicationConfig(
    val environment: String,
    val frontendUrl: String
)

class ConfigLoader {
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)
    private val dotenv = dotenv {
        ignoreIfMissing = true
        systemProperties = true
    }

    fun load(): AppConfig {
        logger.info("Loading application configuration...")

        val databaseConfig = DatabaseConfig(
            uri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017/kliksewa_db"
        )

        val redisConfig = RedisConfig(
            host = dotenv["REDIS_HOST"] ?: "localhost",
            port = dotenv["REDIS_PORT"]?.toIntOrNull() ?: 6379,
            password = dotenv["REDIS_PASSWORD"]?.takeIf { it.isNotBlank() }
        )

        val emailConfig = EmailConfig(
            apiKey = dotenv["BREVO_API_KEY"] ?: run {
                logger.warn("BREVO_API_KEY not set. Email functionality will be disabled.")
                ""
            },
            senderEmail = dotenv["SENDER_EMAIL"] ?: "noreply@kliksewa.com",
            senderName = dotenv["SENDER_NAME"] ?: "Klik Sewa"
        )

        val jwtConfig = JwtConfig(
            secret = dotenv["JWT_SECRET"] ?: run {
                logger.error("JWT_SECRET environment variable is not set!")
                throw IllegalStateException("JWT_SECRET environment variable is not set!")
            },
            expirationInMinutes = dotenv["JWT_EXPIRATION_IN_MINUTES"]?.toLongOrNull() ?: 10080L
        )

        val appConfig = ApplicationConfig(
            environment = dotenv["APP_ENV"] ?: "development",
            frontendUrl = dotenv["FRONTEND_URL"] ?: "http://localhost:3000"
        )

        logger.info("Configuration loaded successfully.")
        logger.info("Environment: ${appConfig.environment}")
        logger.info("Database URI: ${databaseConfig.uri}")
        logger.info("Redis Host: ${redisConfig.host}:${redisConfig.port}")
        logger.info("Frontend URL: ${appConfig.frontendUrl}")

        return AppConfig(databaseConfig, redisConfig, emailConfig, jwtConfig, appConfig)
    }
}
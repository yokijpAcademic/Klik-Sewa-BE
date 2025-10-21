package com.gity.config

import io.github.cdimascio.dotenv.dotenv // Tambahkan dependensi dotenv jika belum
import org.slf4j.LoggerFactory

data class AppConfig(
    val database: DatabaseConfig,
    val redis: RedisConfig,
    val email: EmailConfig,
    val jwt: JwtConfig
    // Tambahkan konfigurasi lain seperti Cloudinary nanti
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

class ConfigLoader {
    private val logger = LoggerFactory.getLogger(ConfigLoader::class.java)
    private val dotenv = dotenv { ignoreIfMissing = true } // Baca .env jika ada

    fun load(): AppConfig {
        logger.info("Loading application configuration...")

        val databaseConfig = DatabaseConfig(
            uri = dotenv["MONGODB_URI"] ?: "mongodb://localhost:27017/kliksewa_db" // Default jika tidak ada
        )

        val redisConfig = RedisConfig(
            host = dotenv["REDIS_HOST"] ?: "localhost",
            port = dotenv["REDIS_PORT"]?.toIntOrNull() ?: 6379,
            password = dotenv["REDIS_PASSWORD"] // Bisa null
        )

        val emailConfig = EmailConfig(
            apiKey = dotenv["BREVO_API_KEY"] ?: run {
                logger.error("BREVO_API_KEY environment variable is not set!")
                throw IllegalStateException("BREVO_API_KEY environment variable is not set!")
            },
            senderEmail = dotenv["SENDER_EMAIL"] ?: "no-reply@kliksawa.com",
            senderName = dotenv["SENDER_NAME"] ?: "Klik Sewa"
        )

        val jwtConfig = JwtConfig(
            secret = dotenv["JWT_SECRET"] ?: run {
                logger.error("JWT_SECRET environment variable is not set!")
                throw IllegalStateException("JWT_SECRET environment variable is not set!")
            },
            expirationInMinutes = dotenv["JWT_EXPIRATION_IN_MINUTES"]?.toLongOrNull() ?: 10080L // 7 hari default
        )

        logger.info("Configuration loaded successfully.")
        return AppConfig(databaseConfig, redisConfig, emailConfig, jwtConfig)
    }
}
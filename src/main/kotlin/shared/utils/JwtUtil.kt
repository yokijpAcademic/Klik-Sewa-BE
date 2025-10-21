package com.gity.shared.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.gity.config.AppConfig
import java.util.Date

class JwtUtil(
    private val appConfig: AppConfig
) {
    private val algorithm = Algorithm.HMAC256(appConfig.jwt.secret)

    private val verifier: JWTVerifier = JWT.require(algorithm).build()

    fun generateToken(userId: String, role: String): String {
        val expiresAt = Date(System.currentTimeMillis() + (appConfig.jwt.expirationInMinutes * 60000L))
        return JWT.create()
            .withSubject(userId)
            .withClaim("role", role)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun verifyToken(token: String): String? { // Return user ID jika valid, null jika tidak
        return try {
            val decodedJWT = verifier.verify(token)
            decodedJWT.subject
        } catch (e: Exception) {
            null // Token tidak valid
        }
    }

    fun getRoleFromToken(token: String): String? {
        return try {
            val decodedJWT = verifier.verify(token)
            decodedJWT.getClaim("role").asString()
        } catch (e: Exception) {
            null // Token tidak valid
        }
    }
}
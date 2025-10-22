package com.gity.shared.utils

import org.mindrot.jbcrypt.BCrypt
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom
import java.util.*
import kotlin.math.log

class HashingUtil {

    /**
     * Hash password menggunakan BCrypt
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    /**
     * Verifikasi password dengan hash
     */
    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(password, hashedPassword)
        } catch (e: Exception) {
//            berikan log

            false
        }
    }

    /**
     * Generate random token untuk email verification / password reset
     * @param length panjang token (default 32 characters)
     */
    fun generateToken(length: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /**
     * Generate SHA-256 hash untuk verification token
     */
    fun hashToken(token: String): String {
        return DigestUtils.sha256Hex(token)
    }
}
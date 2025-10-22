package com.gity.shared.models

import com.gity.shared.models.enums.UserRole
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

@Serializable
data class User(
    @BsonId
    @Contextual
    val _id: ObjectId? = null,
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole = UserRole.USER,
    val isActive: Boolean = true,
    val emailVerified: Boolean = false,
    val emailVerificationToken: String? = null,
    val emailVerificationExpires: Long? = null,
    val passwordResetToken: String? = null,
    val passwordResetExpires: Long? = null,
    val createdAt: Long = Instant.now().epochSecond,
    val updatedAt: Long = Instant.now().epochSecond
) {
    // Helper property untuk mendapatkan ID sebagai string
    val id: String?
        get() = _id?.toHexString()
}
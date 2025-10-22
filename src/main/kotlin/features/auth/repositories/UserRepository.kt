package com.gity.features.auth.repositories

import com.gity.config.DatabaseClient
import com.gity.shared.models.User
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import java.time.Instant

class UserRepository(
    private val databaseClient: DatabaseClient
) {
    private val collection: MongoCollection<User> = databaseClient.getCollection("users")

    /**
     * Mencari user berdasarkan email
     */
    suspend fun findByEmail(email: String): User? {
        return collection.find(Filters.eq("email", email)).firstOrNull()
    }

    /**
     * Mencari user berdasarkan ID
     */
    suspend fun findById(id: String): User? {
        return try {
            val objectId = ObjectId(id)
            collection.find(Filters.eq("_id", objectId)).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mencari user berdasarkan email verification token
     */
    suspend fun findByEmailVerificationToken(token: String): User? {
        return collection.find(
            Filters.and(
                Filters.eq("emailVerificationToken", token),
                Filters.gt("emailVerificationExpires", Instant.now().epochSecond)
            )
        ).firstOrNull()
    }

    /**
     * Mencari user berdasarkan password reset token
     */
    suspend fun findByPasswordResetToken(token: String): User? {
        return collection.find(
            Filters.and(
                Filters.eq("passwordResetToken", token),
                Filters.gt("passwordResetExpires", Instant.now().epochSecond)
            )
        ).firstOrNull()
    }

    /**
     * Membuat user baru
     */
    suspend fun create(user: User): User? {
        return try {
            val result = collection.insertOne(user)
            if (result.insertedId != null) {
                user.copy(_id = result.insertedId!!.asObjectId().value)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Update user berdasarkan ID
     */
    suspend fun updateById(id: String, updates: Map<String, Any?>): Boolean {
        return try {
            val objectId = ObjectId(id)
            val updateDoc = Updates.combine(
                updates.map { (key, value) ->
                    Updates.set(key, value)
                } + Updates.set("updatedAt", Instant.now().epochSecond)
            )

            val result = collection.updateOne(
                Filters.eq("_id", objectId),
                updateDoc
            )
            result.modifiedCount > 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifikasi email user
     */
    suspend fun verifyEmail(id: String): Boolean {
        return updateById(
            id,
            mapOf(
                "emailVerified" to true,
                "emailVerificationToken" to null,
                "emailVerificationExpires" to null
            )
        )
    }

    /**
     * Update password user
     */
    suspend fun updatePassword(id: String, hashedPassword: String): Boolean {
        return updateById(
            id,
            mapOf(
                "password" to hashedPassword,
                "passwordResetToken" to null,
                "passwordResetExpires" to null
            )
        )
    }

    /**
     * Set email verification token
     */
    suspend fun setEmailVerificationToken(id: String, token: String, expiresAt: Long): Boolean {
        return updateById(
            id,
            mapOf(
                "emailVerificationToken" to token,
                "emailVerificationExpires" to expiresAt
            )
        )
    }

    /**
     * Set password reset token
     */
    suspend fun setPasswordResetToken(id: String, token: String, expiresAt: Long): Boolean {
        return updateById(
            id,
            mapOf(
                "passwordResetToken" to token,
                "passwordResetExpires" to expiresAt
            )
        )
    }

    /**
     * Cek apakah email sudah ada
     */
    suspend fun emailExists(email: String): Boolean {
        return findByEmail(email) != null
    }

    /**
     * Delete user berdasarkan ID
     */
    suspend fun deleteById(id: String): Boolean {
        return try {
            val objectId = ObjectId(id)
            val result = collection.deleteOne(Filters.eq("_id", objectId))
            result.deletedCount > 0
        } catch (e: Exception) {
            false
        }
    }
}
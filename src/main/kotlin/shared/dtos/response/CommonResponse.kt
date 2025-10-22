package com.gity.shared.dtos.response

import kotlinx.serialization.Serializable
import redis.clients.jedis.search.Query


@Serializable
data class CommonResponse<T> (
    val success: Boolean,
    val message: String,
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): CommonResponse<T> {
            return CommonResponse(
                success = true,
                message = message,
                data = data
            )
        }

        fun <T> error(message: String): CommonResponse<T> {
            return CommonResponse(
                success = false,
                message = message,
                data = null
            )
        }
    }
}

@Serializable
data class PaginatedResponse<T> (
    val success: Boolean,
    val message : String,
    val data: List<T>,
    val pagination: PaginationMeta
)

@Serializable
data class PaginationMeta(
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPage: Int
)


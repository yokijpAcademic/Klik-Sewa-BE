package com.gity.shared.dtos.response

import kotlinx.serialization.Serializable
import redis.clients.jedis.search.Query


@Serializable
data class CommonResponse<T> (
    val success: Boolean,
    val message: String,
    val data: T? = null
)

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


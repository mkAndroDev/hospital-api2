package com.example.er.dto

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Long,
    val limit: Int,
    val offset: Int
)

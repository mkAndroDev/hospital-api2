package com.example.er.dto

import com.example.er.model.Status
import kotlinx.serialization.Serializable

@Serializable
data class UpdateStatusRequest(
    val status: Status
)
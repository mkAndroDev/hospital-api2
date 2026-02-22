package com.example.er.dto

import com.example.er.model.Condition
import com.example.er.model.Status
import kotlinx.serialization.Serializable

@Serializable
data class PatientRequest(
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val condition: Condition,
    val status: Status? = null
)

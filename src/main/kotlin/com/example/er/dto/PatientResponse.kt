package com.example.er.dto

import com.example.er.model.Condition
import com.example.er.model.Patient
import com.example.er.model.Status
import kotlinx.serialization.Serializable

@Serializable
data class PatientResponse(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val condition: Condition,
    val status: Status,
    val admittedAt: String
)

fun Patient.toResponse() = PatientResponse(
    id = id,
    firstName = firstName,
    lastName = lastName,
    pesel = pesel,
    condition = condition,
    status = status,
    admittedAt = admittedAt.toString()
)

package com.example.er.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Patients : LongIdTable("patients") {
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val pesel = varchar("pesel", 11).uniqueIndex()
    val condition = varchar("condition", 20)
    val status = varchar("status", 20)
    val admittedAt = datetime("admitted_at")
}

data class Patient(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val condition: Condition,
    val status: Status,
    val admittedAt: LocalDateTime
)

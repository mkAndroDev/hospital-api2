package com.example.er.repository

import com.example.er.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class PatientRepository {

    fun save(patient: Patient): Patient = transaction {
        val id = Patients.insertAndGetId {
            it[firstName] = patient.firstName
            it[lastName] = patient.lastName
            it[pesel] = patient.pesel
            it[condition] = patient.condition.name
            it[status] = patient.status.name
            it[admittedAt] = patient.admittedAt
        }

        patient.copy(id = id.value)
    }

    fun create(
        firstName: String,
        lastName: String,
        pesel: String,
        condition: Condition,
        status: Status,
        admittedAt: LocalDateTime
    ): Patient = transaction {
        val id = Patients.insertAndGetId {
            it[Patients.firstName] = firstName
            it[Patients.lastName] = lastName
            it[Patients.pesel] = pesel
            it[Patients.condition] = condition.name
            it[Patients.status] = status.name
            it[Patients.admittedAt] = admittedAt
        }

        Patient(
            id = id.value,
            firstName = firstName,
            lastName = lastName,
            pesel = pesel,
            condition = condition,
            status = status,
            admittedAt = admittedAt
        )
    }

    fun findById(id: Long): Patient? = transaction {
        Patients.select { Patients.id eq id }
            .map { it.toPatient() }
            .singleOrNull()
    }

    fun findByPesel(pesel: String): Patient? = transaction {
        Patients.select { Patients.pesel eq pesel }
            .map { it.toPatient() }
            .singleOrNull()
    }

    fun findByStatus(status: Status): List<Patient> = transaction {
        Patients.select { Patients.status eq status.name }
            .map { it.toPatient() }
    }

    fun update(patient: Patient): Patient = transaction {
        Patients.update({ Patients.id eq patient.id }) {
            it[firstName] = patient.firstName
            it[lastName] = patient.lastName
            it[pesel] = patient.pesel
            it[condition] = patient.condition.name
            it[status] = patient.status.name
            it[admittedAt] = patient.admittedAt
        }

        patient
    }

    fun updateStatus(id: Long, newStatus: Status): Patient? = transaction {
        val updated = Patients.update({ Patients.id eq id }) {
            it[status] = newStatus.name
        }

        if (updated > 0) findById(id) else null
    }

    fun findAll(
        statusFilter: Status? = null,
        conditionFilter: Condition? = null,
        limit: Int = 100,
        offset: Int = 0,
        sortOrder: String = "desc"
    ): List<Patient> = transaction {
        var query = Patients.selectAll()

        statusFilter?.let { query = query.andWhere { Patients.status eq it.name } }
        conditionFilter?.let { query = query.andWhere { Patients.condition eq it.name } }

        val order = if (sortOrder.lowercase() == "asc") SortOrder.ASC else SortOrder.DESC
        query.orderBy(Patients.admittedAt, order)
            .limit(limit, offset.toLong())
            .map { it.toPatient() }
    }

    fun count(statusFilter: Status? = null, conditionFilter: Condition? = null): Long = transaction {
        var query = Patients.selectAll()

        statusFilter?.let { query = query.andWhere { Patients.status eq it.name } }
        conditionFilter?.let { query = query.andWhere { Patients.condition eq it.name } }

        query.count()
    }

    private fun ResultRow.toPatient() = Patient(
        id = this[Patients.id].value,
        firstName = this[Patients.firstName],
        lastName = this[Patients.lastName],
        pesel = this[Patients.pesel],
        condition = Condition.valueOf(this[Patients.condition]),
        status = Status.valueOf(this[Patients.status]),
        admittedAt = this[Patients.admittedAt]
    )
}
package com.example.er.service

import com.example.er.dto.PatientRequest
import com.example.er.model.Condition
import com.example.er.model.Patient
import com.example.er.model.Status
import com.example.er.repository.PatientRepository
import java.time.LocalDateTime
import java.time.ZoneOffset

class PatientService(
    private val patientRepository: PatientRepository
) {

    fun admitPatient(request: PatientRequest): Result<Patient> = runCatching {
        if (!isValidPesel(request.pesel)) {
            throw ValidationException("Invalid PESEL format")
        }

        patientRepository.findByPesel(request.pesel)?.let {
            throw DuplicatePeselException("Patient with PESEL ${request.pesel} already exists")
        }

        if (request.condition !in Condition.entries) {
            throw ValidationException("Invalid condition")
        }

        val patient = patientRepository.create(
            request.firstName,
            request.lastName,
            request.pesel,
            request.condition,
            request.status ?: Status.NEW,
            LocalDateTime.now(ZoneOffset.UTC)
        )

        patientRepository.save(patient)
    }

    fun updatePatientStatus(id: Long, newStatus: Status): Result<Patient> = runCatching {
        val patient = patientRepository.findById(id)
            ?: throw NotFoundException("Patient with ID $id not found")

        val updatedPatient = patient.copy(status = newStatus)
        patientRepository.update(updatedPatient)
    }

    fun getPatients(
        status: Status?,
        condition: Condition?,
        limit: Int,
        offset: Int,
        sort: String
    ): Pair<List<Patient>, Long> {
        val patients = patientRepository.findAll(status, condition, limit, offset, sort)
        val total = patientRepository.count(status, condition)
        return patients to total
    }

    fun getNewPatients(): List<Patient> {
        return patientRepository.findByStatus(Status.NEW)
    }

    private fun isValidPesel(pesel: String): Boolean {
        if (pesel.length != 11 || !pesel.all { it.isDigit() }) {
            return false
        }

        val weights = intArrayOf(1, 3, 7, 9, 1, 3, 7, 9, 1, 3)
        val checksum = pesel.take(10)
            .mapIndexed { index, char -> char.digitToInt() * weights[index] }
            .sum() % 10

        val controlDigit = (10 - checksum) % 10
        return controlDigit == pesel[10].digitToInt()
    }
}

// Custom exceptions
class ValidationException(message: String) : Exception(message)
class DuplicatePeselException(message: String) : Exception(message)
class NotFoundException(message: String) : Exception(message)
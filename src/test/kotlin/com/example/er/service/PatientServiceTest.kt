package com.example.er.service

import com.example.er.dto.PatientRequest
import com.example.er.model.*
import com.example.er.repository.PatientRepository
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class PatientServiceTest {
    
    private lateinit var repository: PatientRepository
    private lateinit var service: PatientService
    
    @BeforeEach
    fun setup() {
        repository = mockk()
        service = PatientService(repository)
    }
    
    @AfterEach
    fun teardown() {
        clearAllMocks()
    }
    
    @Test
    fun `should admit patient with valid data`() {
        val request = PatientRequest(
            firstName = "John",
            lastName = "Doe",
            pesel = "12345678901",
            condition = Condition.RED
        )
        
        val expectedPatient = Patient(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            pesel = "12345678901",
            condition = Condition.RED,
            status = Status.NEW,
            admittedAt = LocalDateTime.now()
        )
        
        every { repository.findByPesel("12345678901") } returns null
        every { 
            repository.create(any(), any(), any(), any(), any(), any())
        } returns expectedPatient
        
        val result = service.admitPatient(request)
        
        assertTrue(result.isSuccess)
        assertEquals("John", result.getOrNull()?.firstName)
    }
    
    @Test
    fun `should reject invalid PESEL format`() {
        val request = PatientRequest(
            firstName = "Invalid",
            lastName = "Patient",
            pesel = "123",
            condition = Condition.GREEN
        )
        
        val result = service.admitPatient(request)
        
        assertTrue(result.isFailure)
        assertInstanceOf(ValidationException::class.java, result.exceptionOrNull())
    }
    
    @Test
    fun `should reject duplicate PESEL`() {
        val request = PatientRequest(
            firstName = "John",
            lastName = "Duplicate",
            pesel = "12345678901",
            condition = Condition.ORANGE
        )
        
        val existingPatient = Patient(
            id = 1L,
            firstName = "John",
            lastName = "Existing",
            pesel = "12345678901",
            condition = Condition.RED,
            status = Status.NEW,
            admittedAt = LocalDateTime.now()
        )
        
        every { repository.findByPesel("12345678901") } returns existingPatient
        
        val result = service.admitPatient(request)
        
        assertTrue(result.isFailure)
        assertInstanceOf(DuplicatePeselException::class.java, result.exceptionOrNull())
    }
}

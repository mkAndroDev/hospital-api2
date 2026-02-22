package com.example.er.routes

import com.example.er.dto.*
import com.example.er.model.Condition
import com.example.er.model.Status
import com.example.er.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configurePatientRoutes() {
    val patientService by inject<PatientService>()

    routing {
        // Protect all patient routes with JWT authentication
        authenticate("auth-jwt") {
            route("/patients") {

                // POST /patients - Admit a patient
                post {
                    val request = call.receive<PatientRequest>()

                    patientService.admitPatient(request)
                        .onSuccess { patient ->
                            call.respond(HttpStatusCode.Created, patient.toResponse())
                        }
                        .onFailure { exception ->
                            when (exception) {
                                is ValidationException -> {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Validation error", "VALIDATION_ERROR")
                                    )
                                }
                                is DuplicatePeselException -> {
                                    call.respond(
                                        HttpStatusCode.Conflict,
                                        ErrorResponse(exception.message ?: "Duplicate PESEL", "DUPLICATE_PESEL")
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse("Internal server error", "INTERNAL_ERROR")
                                    )
                                }
                            }
                        }
                }

                // PUT /patients/{id}/status - Update patient status
                put("/{id}/status") {
                    val id = call.parameters["id"]?.toLongOrNull()

                    if (id == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid patient ID", "INVALID_ID")
                        )
                        return@put
                    }

                    val request = try {
                        call.receive<UpdateStatusRequest>()
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Invalid request body", "INVALID_BODY")
                        )
                        return@put
                    }

                    patientService.updatePatientStatus(id, request.status)
                        .onSuccess { patient ->
                            call.respond(HttpStatusCode.OK, patient.toResponse())
                        }
                        .onFailure { exception ->
                            when (exception) {
                                is NotFoundException -> {
                                    call.respond(
                                        HttpStatusCode.NotFound,
                                        ErrorResponse(exception.message ?: "Patient not found", "NOT_FOUND")
                                    )
                                }
                                is ValidationException -> {
                                    call.respond(
                                        HttpStatusCode.BadRequest,
                                        ErrorResponse(exception.message ?: "Invalid status", "INVALID_STATUS")
                                    )
                                }
                                else -> {
                                    call.respond(
                                        HttpStatusCode.InternalServerError,
                                        ErrorResponse("Internal server error", "INTERNAL_ERROR")
                                    )
                                }
                            }
                        }
                }

                // GET /patients
                get {
                    val statusParam = call.request.queryParameters["status"]
                    val conditionParam = call.request.queryParameters["condition"]
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                    val sort = call.request.queryParameters["sort"] ?: "desc"

                    val status = statusParam?.let {
                        try { Status.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    }
                    val condition = conditionParam?.let {
                        try { Condition.valueOf(it.uppercase()) } catch (e: Exception) { null }
                    }

                    val (patients, total) = patientService.getPatients(status, condition, limit, offset, sort)

                    val response = PaginatedResponse(
                        data = patients.map { it.toResponse() },
                        total = total,
                        limit = limit,
                        offset = offset
                    )

                    call.respond(HttpStatusCode.OK, response)
                }

                // GET /patients/new
                get("/new") {
                    val patients = patientService.getNewPatients()
                    call.respond(HttpStatusCode.OK, patients.map { it.toResponse() })
                }
            }
        }
    }
}
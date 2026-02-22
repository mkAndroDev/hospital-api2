package com.example.er.di

import com.example.er.repository.PatientRepository
import com.example.er.repository.UserRepository
import com.example.er.service.AuthService
import com.example.er.service.PatientService
import org.koin.dsl.module

val appModule = module {
    single { PatientRepository() }
    single { UserRepository() }
    single { PatientService(get()) }
    single { AuthService(get()) }
}
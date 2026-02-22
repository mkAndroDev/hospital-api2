package com.example.er.util

import org.mindrot.jbcrypt.BCrypt

fun main() {
    println("=== Password Hash Generator ===\n")

    val passwords = listOf("admin123", "doctor123", "nurse123")

    passwords.forEach { password ->
        val hash = BCrypt.hashpw(password, BCrypt.gensalt(10))
        println("Password: $password")
        println("Hash: $hash")
        println()
    }

    // Test verification
    println("=== Testing Verification ===")
    val testPassword = "admin123"
    val testHash = BCrypt.hashpw(testPassword, BCrypt.gensalt(10))
    println("Test password: $testPassword")
    println("Verification: ${BCrypt.checkpw(testPassword, testHash)}")
}
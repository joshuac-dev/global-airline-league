package com.gal.core.airline

import com.gal.core.AirlineId
import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * Airline domain entity.
 * Represents an airline company in the simulation with minimal identity fields.
 */
@Serializable
data class Airline(
    val id: AirlineId,
    val name: String,
    val createdAtEpochSeconds: Long
) {
    init {
        require(name.isNotBlank()) { "Airline name cannot be blank" }
        require(createdAtEpochSeconds >= 0) { "Created at timestamp must be non-negative" }
        validateAirlineName(name).getOrThrow()
    }
    
    /**
     * Get the createdAt timestamp as an Instant.
     */
    val createdAt: Instant
        get() = Instant.ofEpochSecond(createdAtEpochSeconds)
}

/**
 * Result type for validation operations.
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Failure(val message: String) : ValidationResult()
    
    fun isSuccess(): Boolean = this is Success
    fun getOrThrow(): Unit = when (this) {
        is Success -> Unit
        is Failure -> throw IllegalArgumentException(message)
    }
}

/**
 * Validates airline name according to business rules.
 * - Name must be trimmed
 * - Length must be between 3 and 40 characters
 * - Allowed characters: letters, numbers, space, hyphen, apostrophe, ampersand, dot
 * 
 * @param name The airline name to validate
 * @return ValidationResult indicating success or failure with message
 */
fun validateAirlineName(name: String): ValidationResult {
    val trimmedName = name.trim()
    
    // Check if name was already trimmed
    if (name != trimmedName) {
        return ValidationResult.Failure("Airline name must be trimmed")
    }
    
    // Check length
    if (trimmedName.length < 3) {
        return ValidationResult.Failure("Airline name must be at least 3 characters long")
    }
    if (trimmedName.length > 40) {
        return ValidationResult.Failure("Airline name must not exceed 40 characters")
    }
    
    // Check allowed characters: letters, numbers, space, hyphen, apostrophe, ampersand, dot
    val allowedCharactersRegex = Regex("^[a-zA-Z0-9 \\-'.&]+$")
    if (!allowedCharactersRegex.matches(trimmedName)) {
        return ValidationResult.Failure("Airline name contains invalid characters. Allowed: letters, numbers, space, hyphen, apostrophe, ampersand, dot")
    }
    
    return ValidationResult.Success
}

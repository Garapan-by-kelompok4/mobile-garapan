package com.app.garapan.domain.validation

object PasswordValidator {
    fun isValid(password: String): Boolean =
        password.length >= 8 &&
            password.any { it.isLowerCase() } &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }
}

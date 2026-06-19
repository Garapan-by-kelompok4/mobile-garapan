package com.app.garapan.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `valid password requires minimum length lowercase uppercase number and symbol`() {
        assertTrue(PasswordValidator.isValid("Strong1!"))
    }

    @Test
    fun `password without required character classes is invalid`() {
        assertFalse(PasswordValidator.isValid("short1!"))
        assertFalse(PasswordValidator.isValid("NOLOWERCASE1!"))
        assertFalse(PasswordValidator.isValid("nouppercase1!"))
        assertFalse(PasswordValidator.isValid("NoNumber!"))
        assertFalse(PasswordValidator.isValid("NoSymbol1"))
    }
}

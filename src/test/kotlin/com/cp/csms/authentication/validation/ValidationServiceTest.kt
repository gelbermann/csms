package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ValidationServiceTest {

    @Mock
    private lateinit var validator1: AuthenticationValidator

    @Mock
    private lateinit var validator2: AuthenticationValidator

    @Test
    fun `should return true when all validators pass`() {
        val message = AuthenticationMessage(REQUEST_ID, "valid-token")
        whenever(validator1.validate(message)).thenReturn(true)
        whenever(validator2.validate(message)).thenReturn(true)

        val validationService = ValidationService(listOf(validator1, validator2))

        val result = validationService.isValid(message)

        assertThat(result).isTrue()
    }

    @Test
    fun `should short circuit on first failure`() {
        val message = AuthenticationMessage(REQUEST_ID, "invalid-token")

        val failingValidator = object : AuthenticationValidator {
            override fun validate(message: AuthenticationMessage?): Boolean = false
        }
        val throwingValidator = object : AuthenticationValidator {
            override fun validate(message: AuthenticationMessage?): Boolean {
                throw RuntimeException("Should not be called due to short-circuit")
            }
        }

        val shortCircuitService = ValidationService(listOf(failingValidator, throwingValidator))

        val result = shortCircuitService.isValid(message)

        assertThat(result).isFalse()
    }

    companion object {
        private const val REQUEST_ID = "req-123"
    }
}

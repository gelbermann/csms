package com.cp.csms.authentication.validation

import com.cp.csms.common.AuthenticationMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TokenLengthValidatorTest {

    @InjectMocks
    private lateinit var validator: TokenLengthValidator

    @Test
    fun `should return true when token length is minimum`() {
        val token = "a".repeat(20)
        val message = AuthenticationMessage(REQUEST_ID, token)

        val result = validator.validate(message)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when token length is maximum`() {
        val token = "a".repeat(80)
        val message = AuthenticationMessage(REQUEST_ID, token)

        val result = validator.validate(message)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return true when token length is within range`() {
        val token = "a".repeat(50)
        val message = AuthenticationMessage(REQUEST_ID, token)

        val result = validator.validate(message)

        assertThat(result).isTrue()
    }

    @Test
    fun `should return false when token is too short`() {
        val token = "a".repeat(19)
        val message = AuthenticationMessage(REQUEST_ID, token)

        val result = validator.validate(message)

        assertThat(result).isFalse()
    }

    @Test
    fun `should return false when token is too long`() {
        val token = "a".repeat(81)
        val message = AuthenticationMessage(REQUEST_ID, token)

        val result = validator.validate(message)

        assertThat(result).isFalse()
    }

    companion object {
        private const val REQUEST_ID = "req-123"
    }
}

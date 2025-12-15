package com.cp.csms.authentication

import com.cp.csms.common.AuthenticationStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
class AuthenticationServiceTest {

    @Mock
    private lateinit var tokenStatusProvider: TokenStatusProvider

    @InjectMocks
    private lateinit var authenticationService: AuthenticationService

    @Test
    fun `should return accepted when token is enabled`() {
        val token = "enabled-token"
        whenever(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.of(true))

        val result = authenticationService.authenticate(token)

        assertThat(result).isEqualTo(AuthenticationStatus.ACCEPTED)
    }

    @Test
    fun `should return rejected when token is disabled`() {
        val token = "disabled-token"
        whenever(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.of(false))

        val result = authenticationService.authenticate(token)

        assertThat(result).isEqualTo(AuthenticationStatus.REJECTED)
    }

    @Test
    fun `should return unknown when token is not found`() {
        val token = "unknown-token"
        whenever(tokenStatusProvider.isTokenEnabled(token)).thenReturn(Optional.empty())

        val result = authenticationService.authenticate(token)

        assertThat(result).isEqualTo(AuthenticationStatus.UNKNOWN)
    }
}

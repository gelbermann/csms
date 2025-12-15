package com.cp.csms.transactions.controllers

import com.cp.csms.transactions.AuthorizationRequest
import com.cp.csms.transactions.AuthorizationResponse
import com.cp.csms.transactions.AuthorizationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeoutException

@RestController
@RequestMapping("api/v1/transaction")
class TransactionController(
    private val authorizationService: AuthorizationService
) {

    @PostMapping("/authorize")
    fun authorizeTransaction(
        @RequestBody request: AuthorizationRequest
    ): ResponseEntity<AuthorizationResponse> {
        log.info("Authorizing transaction for request: {}", request)

        return try {
            val authenticationStatus = authorizationService.authorize(request)
            log.info("Authorization result for request {}: {}", request, authenticationStatus)
            ResponseEntity.ok(AuthorizationResponse(authenticationStatus))
        } catch (e: TimeoutException) {
            ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(TransactionController::class.java)
    }
}

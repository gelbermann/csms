package com.cp.csms.transactions.controllers;

import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.AuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/transaction")
public class TransactionController {

    private final AuthorizationService authorizationService;

    @PostMapping("/authorize")
    public AuthorizationResponse authorizeTransaction(@RequestBody AuthorizationRequest request) {
        log.info("Authorizing transaction for request: {}", request);

        final var response = authorizationService.authorize(request);

        try {
            AuthResponse response = future.get(5, TimeUnit.SECONDS);
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            pendingRequests.remove(correlationId);
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                    .body(new AuthResponse("Timeout"));
        } catch (Exception e) {
            pendingRequests.remove(correlationId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("Error"));
        }
    }
}

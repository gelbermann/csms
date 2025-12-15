package com.cp.csms.transactions.controllers;

import com.cp.csms.common.AuthenticationStatus;
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

import java.util.concurrent.TimeoutException;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/transaction")
public class TransactionController {

    private final AuthorizationService authorizationService;

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorizeTransaction(@RequestBody AuthorizationRequest request) {
        log.info("Authorizing transaction for request: {}", request);

        try {
            final AuthenticationStatus authenticationStatus = authorizationService.authorize(request);
            log.info("Authorization result for request {}: {}", request, authenticationStatus);
            return ResponseEntity.ok(
                    AuthorizationResponse.builder()
                            .authenticationStatus(authenticationStatus)
                            .build()
            );
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

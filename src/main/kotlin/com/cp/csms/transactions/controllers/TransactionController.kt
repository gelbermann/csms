package com.cp.csms.transactions.controllers;

import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeoutException;


@RestController
@RequestMapping(value = "api/v1/transaction")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final AuthorizationService authorizationService;

    public TransactionController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping("/authorize")
    public ResponseEntity<AuthorizationResponse> authorizeTransaction(@RequestBody AuthorizationRequest request) {
        log.info("Authorizing transaction for request: {}", request);

        try {
            final AuthenticationStatus authenticationStatus = authorizationService.authorize(request);
            log.info("Authorization result for request {}: {}", request, authenticationStatus);
            return ResponseEntity.ok(new AuthorizationResponse(authenticationStatus));
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}

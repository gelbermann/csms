package com.cp.csms.transactions.controllers;

import com.cp.csms.common.AuthenticationMessage;
import com.cp.csms.common.AuthenticationResponse;
import com.cp.csms.common.AuthenticationStatus;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.AuthorizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private static final int TIMEOUT_SECONDS = 5;

    // TODO think if concurrent map is the best way to handle pending requests
    private final Map<String, CompletableFuture<AuthenticationResponse>> pendingRequests = new ConcurrentHashMap<>();

    // TODO extract to dedicated producer class
    private final KafkaTemplate<String, AuthenticationMessage> kafkaProducer;

    public Optional<AuthenticationStatus> authorize(AuthorizationRequest request) {
        final String requestId = UUID.randomUUID().toString();
        final CompletableFuture<AuthenticationResponse> future = new CompletableFuture<>();

        pendingRequests.put(requestId, future);

        final AuthenticationMessage message = AuthenticationMessage.builder()
                .requestId(requestId)
                .token(request.getDriverIdentifier().getId())
                .build();

        kafkaProducer.send("auth-request", requestId, message); // TODO extract topic name to yaml/config class

        try {
            final AuthenticationResponse response = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return Optional.of(response.getStatus());
        } catch (TimeoutException e) {
            pendingRequests.remove(requestId);
            log.warn("Authorization request timed out for requestId: {}", requestId);
            return Optional.empty();
        } catch (Exception e) {
            pendingRequests.remove(requestId);
            log.error("Error while authorizing requestId: {}", requestId, e);
            return Optional.empty();
        }
    }

    @KafkaListener(topics = "auth-response", groupId = "transaction-service") // TODO extract names to yaml/config class
    public void handleAuthResponse(AuthenticationResponse response) {
        CompletableFuture<AuthenticationResponse> future = pendingRequests.remove(response.getRequestId());

        if (future != null && !future.isDone()) {
            future.complete(response);
        }
    }

}

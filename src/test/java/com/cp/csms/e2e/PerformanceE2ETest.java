package com.cp.csms.e2e;

import com.cp.csms.e2e.support.TestDataBuilder;
import com.cp.csms.transactions.AuthorizationRequest;
import com.cp.csms.transactions.AuthorizationResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Performance and reliability E2E tests.
 * Tests system behavior under load and concurrent requests.
 */
public class PerformanceE2ETest extends BaseE2ETest {

    private static final Logger log = LoggerFactory.getLogger(PerformanceE2ETest.class);

    @Test
    public void shouldHandleHighVolumeRequests() {

        final int requestCount = 100;
        List<Long> responseTimes = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {
            AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                    "station-" + i,
                    "driver-" + i
            );

            Instant start = Instant.now();
            ResponseEntity<AuthorizationResponse> response = restTemplate.postForEntity(
                    getAuthorizationUrl(),
                    request,
                    AuthorizationResponse.class
            );
            Instant end = Instant.now();

            long responseTime = Duration.between(start, end).toMillis();
            responseTimes.add(responseTime);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAuthenticationStatus()).isNotNull();
        }

        double averageResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);

        long maxResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0);

        log.info("Processed {} requests - Average: {}ms, Max: {}ms", requestCount, averageResponseTime, maxResponseTime);

        assertThat(averageResponseTime).isLessThan(5000);
        assertThat(maxResponseTime).isLessThan(15000);
    }

    @Test
    public void shouldHandleMultipleConcurrentRequests() throws InterruptedException, ExecutionException, TimeoutException {

        final int concurrentRequests = 10;
        final ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);
        final List<CompletableFuture<ResponseEntity<AuthorizationResponse>>> futures = new ArrayList<>();

        final Instant start = Instant.now();
        for (int i = 0; i < concurrentRequests; i++) {
            final int requestId = i;
            CompletableFuture<ResponseEntity<AuthorizationResponse>> future = CompletableFuture.supplyAsync(() -> {
                AuthorizationRequest request = TestDataBuilder.buildAuthorizationRequest(
                        "station-concurrent-" + requestId,
                        "driver-concurrent-" + requestId
                );

                return restTemplate.postForEntity(
                        getAuthorizationUrl(),
                        request,
                        AuthorizationResponse.class
                );
            }, executorService);

            futures.add(future);
        }

        final CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        allFutures.get(30, TimeUnit.SECONDS);
        final Instant end = Instant.now();

        executorService.shutdown();

        for (CompletableFuture<ResponseEntity<AuthorizationResponse>> future : futures) {
            ResponseEntity<AuthorizationResponse> response = future.get();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAuthenticationStatus()).isNotNull();
        }

        final long totalTime = Duration.between(start, end).toMillis();
        log.info("Processed {} concurrent requests in {}ms", concurrentRequests, totalTime);

        assertThat(totalTime).isLessThan(15000);
    }

}

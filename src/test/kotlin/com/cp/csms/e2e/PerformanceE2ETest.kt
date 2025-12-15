package com.cp.csms.e2e

import com.cp.csms.e2e.support.TestDataBuilder
import com.cp.csms.transactions.AuthorizationRequest
import com.cp.csms.transactions.AuthorizationResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Performance and reliability E2E tests.
 * Tests system behavior under load and concurrent requests.
 */
class PerformanceE2ETest : BaseE2ETest() {

    @Test
    fun `should handle high volume requests`() {
        val requestCount = 100
        val responseTimes = mutableListOf<Long>()

        for (i in 0 until requestCount) {
            val request = TestDataBuilder.buildAuthorizationRequest(
                "station-$i",
                "driver-$i"
            )

            val start = Instant.now()
            val response: ResponseEntity<AuthorizationResponse> = restTemplate.postForEntity(
                getAuthorizationUrl(),
                request,
                AuthorizationResponse::class.java
            )
            val end = Instant.now()

            val responseTime = Duration.between(start, end).toMillis()
            responseTimes.add(responseTime)

            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull
            assertThat(response.body?.authenticationStatus).isNotNull
        }

        val averageResponseTime = responseTimes.average()
        val maxResponseTime = responseTimes.maxOrNull() ?: 0

        log.info("Processed {} requests - Average: {}ms, Max: {}ms", requestCount, averageResponseTime, maxResponseTime)

        assertThat(averageResponseTime).isLessThan(5000.0)
        assertThat(maxResponseTime).isLessThan(15000)
    }

    @Test
    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    fun `should handle multiple concurrent requests`() {
        val concurrentRequests = 10
        val executorService = Executors.newFixedThreadPool(concurrentRequests)
        val futures = mutableListOf<CompletableFuture<ResponseEntity<AuthorizationResponse>>>()

        val start = Instant.now()
        for (i in 0 until concurrentRequests) {
            val requestId = i
            val future = CompletableFuture.supplyAsync({
                val request = TestDataBuilder.buildAuthorizationRequest(
                    "station-concurrent-$requestId",
                    "driver-concurrent-$requestId"
                )

                restTemplate.postForEntity(
                    getAuthorizationUrl(),
                    request,
                    AuthorizationResponse::class.java
                )
            }, executorService)

            futures.add(future)
        }

        val allFutures = CompletableFuture.allOf(*futures.toTypedArray())
        allFutures.get(30, TimeUnit.SECONDS)
        val end = Instant.now()

        executorService.shutdown()

        for (future in futures) {
            val response = future.get()
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(response.body).isNotNull
            assertThat(response.body?.authenticationStatus).isNotNull
        }

        val totalTime = Duration.between(start, end).toMillis()
        log.info("Processed {} concurrent requests in {}ms", concurrentRequests, totalTime)

        assertThat(totalTime).isLessThan(15000)
    }

    companion object {
        private val log = LoggerFactory.getLogger(PerformanceE2ETest::class.java)
    }
}

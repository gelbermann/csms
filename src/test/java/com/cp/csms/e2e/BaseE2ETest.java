package com.cp.csms.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

/**
 * Base class for E2E tests with embedded Kafka.
 * Provides shared configuration and common dependencies for all E2E tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        topics = {"auth-request", "auth-response"},
        partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
@TestPropertySource(properties = {
        "logging.level.com.cp.csms=DEBUG",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.enable-auto-commit=true"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    protected RestTemplate restTemplate;
    protected String baseUrl;

    @BeforeEach
    public void setUpBase() {
        baseUrl = "http://localhost:" + port;
        restTemplate = new RestTemplate();
    }

    protected String getAuthorizationUrl() {
        return baseUrl + "/api/v1/transaction/authorize";
    }
}

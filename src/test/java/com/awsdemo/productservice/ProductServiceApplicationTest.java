package com.awsdemo.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void readinessProbeIsUp() {
        ResponseEntity<String> res = rest.getForEntity("/actuator/health/readiness", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("UP");
    }

    @Test
    void livenessProbeIsUp() {
        assertThat(rest.getForEntity("/actuator/health/liveness", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void infoEndpointReportsLocalOutsideEcs() {
        ResponseEntity<String> res = rest.getForEntity("/api/v1/info", String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"taskArn\":\"local\"");
    }

    @Test
    void openApiDocsServed() {
        assertThat(rest.getForEntity("/v3/api-docs", String.class).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }
}

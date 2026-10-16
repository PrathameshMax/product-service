package com.awsdemo.productservice.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Reads ECS Task Metadata Endpoint v4 once at startup. Outside ECS the
 * ECS_CONTAINER_METADATA_URI_V4 variable is absent and values stay "local".
 */
@Component
public class EcsMetadataClient {

    private static final Logger log = LoggerFactory.getLogger(EcsMetadataClient.class);
    private static final String ENV_VAR = "ECS_CONTAINER_METADATA_URI_V4";
    private static final String LOCAL = "local";

    private volatile String taskArn = LOCAL;
    private volatile String availabilityZone = LOCAL;

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        String baseUri = System.getenv(ENV_VAR);
        if (baseUri == null || baseUri.isBlank()) {
            log.info("ECS metadata endpoint not present; running outside ECS");
            return;
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(2));
        try {
            JsonNode task = RestClient.builder()
                    .requestFactory(factory)
                    .build()
                    .get()
                    .uri(baseUri + "/task")
                    .retrieve()
                    .body(JsonNode.class);
            if (task != null) {
                taskArn = task.path("TaskARN").asText(LOCAL);
                availabilityZone = task.path("AvailabilityZone").asText(LOCAL);
            }
            log.info("ECS metadata loaded taskArn={} az={}", taskArn, availabilityZone);
        } catch (RuntimeException ex) {
            log.warn("ECS metadata lookup failed: {}", ex.getMessage());
        }
    }

    public String taskArn() {
        return taskArn;
    }

    public String availabilityZone() {
        return availabilityZone;
    }
}

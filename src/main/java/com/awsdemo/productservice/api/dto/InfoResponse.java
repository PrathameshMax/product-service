package com.awsdemo.productservice.api.dto;

import java.time.Instant;

public record InfoResponse(
        String service,
        String version,
        String environment,
        String hostname,
        String taskArn,
        String availabilityZone,
        Instant startedAt
) {
}

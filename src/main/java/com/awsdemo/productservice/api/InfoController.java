package com.awsdemo.productservice.api;

import com.awsdemo.productservice.api.dto.InfoResponse;
import com.awsdemo.productservice.config.AppProperties;
import com.awsdemo.productservice.config.EcsMetadataClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/info")
@Tag(name = "Info", description = "Instance identity, useful for observing load balancing")
public class InfoController {

    private final AppProperties appProperties;
    private final EcsMetadataClient ecsMetadataClient;
    private final String hostname;
    private final Instant startedAt = Instant.now();

    public InfoController(AppProperties appProperties, EcsMetadataClient ecsMetadataClient) {
        this.appProperties = appProperties;
        this.ecsMetadataClient = ecsMetadataClient;
        this.hostname = resolveHostname();
    }

    @GetMapping
    @Operation(summary = "Which instance served this request")
    public InfoResponse info() {
        return new InfoResponse(
                appProperties.name(),
                appProperties.version(),
                appProperties.environment(),
                hostname,
                ecsMetadataClient.taskArn(),
                ecsMetadataClient.availabilityZone(),
                startedAt
        );
    }

    private static String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "unknown";
        }
    }
}

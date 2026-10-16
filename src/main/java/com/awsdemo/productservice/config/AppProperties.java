package com.awsdemo.productservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("product-service") String name,
        @DefaultValue("local") String version,
        @DefaultValue("local") String environment
) {
}

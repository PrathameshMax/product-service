package com.awsdemo.productservice.api.dto;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        String category
) {
}

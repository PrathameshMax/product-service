package com.awsdemo.productservice.service;

import com.awsdemo.productservice.api.dto.ProductRequest;
import com.awsdemo.productservice.api.dto.ProductResponse;
import com.awsdemo.productservice.api.error.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Static responses only: no persistence. Writes echo the request back;
 * reads always return the same fixed catalogue.
 */
@Service
public class ProductService {

    private static final List<ProductResponse> CATALOGUE = List.of(
            new ProductResponse("1", "Wireless Mouse", new BigDecimal("799.00"), "electronics"),
            new ProductResponse("2", "Mechanical Keyboard", new BigDecimal("3499.00"), "electronics"),
            new ProductResponse("3", "Steel Water Bottle", new BigDecimal("449.00"), "home")
    );

    public List<ProductResponse> findAll() {
        return CATALOGUE;
    }

    public ProductResponse findById(String id) {
        return CATALOGUE.stream()
                .filter(p -> p.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public ProductResponse create(ProductRequest request) {
        return new ProductResponse(UUID.randomUUID().toString(),
                request.name(), request.price(), request.category());
    }

    public ProductResponse update(String id, ProductRequest request) {
        findById(id);
        return new ProductResponse(id, request.name(), request.price(), request.category());
    }

    public void delete(String id) {
        findById(id);
    }
}

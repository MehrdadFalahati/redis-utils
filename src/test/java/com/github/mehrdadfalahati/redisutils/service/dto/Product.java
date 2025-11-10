package com.github.mehrdadfalahati.redisutils.service.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Builder.Default
    private String id = UUID.randomUUID().toString();
    private String name;
    private Double price;
    @Builder.Default
    private Instant createAt = Instant.now();

    public static TypeReference<List<Product>> getTypeReferences() {
        return new TypeReference<List<Product>>() {
        };
    }

    public static TypeReference<Product> getTypeReference() {
        return new TypeReference<Product>() {};
    }
}

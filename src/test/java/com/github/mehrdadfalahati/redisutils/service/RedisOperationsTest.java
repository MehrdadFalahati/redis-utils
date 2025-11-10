package com.github.mehrdadfalahati.redisutils.service;

import com.github.mehrdadfalahati.redisutils.core.RedisKey;
import com.github.mehrdadfalahati.redisutils.operations.RedisKeyOperations;
import com.github.mehrdadfalahati.redisutils.operations.RedisValueOperations;
import com.github.mehrdadfalahati.redisutils.service.dto.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RedisOperationsTest extends AbstractRedisTestContainer {

    @Autowired
    private RedisValueOperations redisValueOperations;

    @Autowired
    private RedisKeyOperations redisKeyOperations;

    private Product milk = Product.builder()
            .name("Milk")
            .price(10.2)
            .build();

    private Product meat = Product.builder()
            .name("Meat")
            .price(100.5)
            .build();

    @Test
    void whenAddingListOfProducts_expectedGetProductsWithKey() {
        List<Product> products = List.of(milk, meat);
        redisValueOperations.set(RedisKey.ofSeconds("products", 5), products);

        @SuppressWarnings("unchecked")
        List<Product> retrievedProducts = redisValueOperations.get("products", List.class);
        assertNotNull(retrievedProducts);
        assertEquals(2, retrievedProducts.size());
    }

    @Test
    void whenAddingListOfProducts_thenCallDeleteByKey_expectedGetNone() {
        List<Product> products = List.of(milk, meat);
        redisValueOperations.set(RedisKey.ofSeconds("products-delete", 5), products);

        long deleted = redisKeyOperations.delete("products-delete");
        assertEquals(1, deleted);

        @SuppressWarnings("unchecked")
        List<Product> retrievedProducts = redisValueOperations.get("products-delete", List.class);
        assertNull(retrievedProducts);
    }

    @Test
    void whenAddingProduct_expectedGetProductWithKey() {
        redisValueOperations.set(RedisKey.ofSeconds("product", 5), milk);

        Product product = redisValueOperations.get("product", Product.class);
        assertNotNull(product);
        assertEquals(milk.getId(), product.getId());
    }

    @Test
    void whenAddingListOfProducts_thenCallHasKey_expectedGetTrue() {
        List<Product> products = List.of(milk, meat);
        redisValueOperations.set(RedisKey.ofSeconds("products-exists", 5), products);

        assertTrue(redisKeyOperations.exists("products-exists"));
    }

    @Test
    void whenSettingIfAbsent_andKeyDoesNotExist_thenReturnTrue() {
        boolean wasSet = redisValueOperations.setIfAbsent(RedisKey.ofSeconds("new-product", 5), milk);
        assertTrue(wasSet);

        Product product = redisValueOperations.get("new-product", Product.class);
        assertNotNull(product);
        assertEquals(milk.getId(), product.getId());
    }

    @Test
    void whenSettingIfAbsent_andKeyExists_thenReturnFalse() {
        redisValueOperations.set(RedisKey.ofSeconds("existing-product", 5), milk);

        boolean wasSet = redisValueOperations.setIfAbsent(RedisKey.ofSeconds("existing-product", 5), meat);
        assertFalse(wasSet);

        Product product = redisValueOperations.get("existing-product", Product.class);
        assertEquals(milk.getId(), product.getId()); // Should still be milk
    }

    @Test
    void whenIncrementing_thenValueIncreases() {
        long value1 = redisValueOperations.increment("counter");
        assertEquals(1, value1);

        long value2 = redisValueOperations.incrementBy("counter", 5);
        assertEquals(6, value2);

        long value3 = redisValueOperations.decrement("counter");
        assertEquals(5, value3);
    }

    @Test
    void whenSettingExpiration_thenKeyHasTtl() {
        redisValueOperations.set(RedisKey.of("temp-key"), milk);

        boolean expired = redisKeyOperations.expire("temp-key", java.time.Duration.ofSeconds(10));
        assertTrue(expired);

        java.time.Duration ttl = redisKeyOperations.ttl("temp-key");
        assertNotNull(ttl);
        assertTrue(ttl.getSeconds() > 0 && ttl.getSeconds() <= 10);
    }
}

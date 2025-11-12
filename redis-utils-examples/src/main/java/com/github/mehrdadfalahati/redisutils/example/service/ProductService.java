package com.github.mehrdadfalahati.redisutils.example.service;

import com.github.mehrdadfalahati.redisutils.operations.RedisHashOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Example service demonstrating RedisHashOperations usage for product catalog.
 *
 * <p>This service shows:
 * <ul>
 *   <li>Hash field operations (hSet, hGet, hGetAll)</li>
 *   <li>Batch hash operations</li>
 *   <li>Hash field existence checks</li>
 *   <li>Increment operations on hash fields</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final RedisHashOperations redisHashOperations;

    private static final String PRODUCT_PREFIX = "product:";
    private static final String INVENTORY_KEY = "inventory";

    /**
     * Store product details as a hash.
     */
    public void saveProduct(String productId, Map<String, Object> productData) {
        String key = PRODUCT_PREFIX + productId;
        redisHashOperations.putAll(key, productData);
        log.info("Saved product: {}", productId);
    }

    /**
     * Get a specific product field.
     */
    public Object getProductField(String productId, String field) {
        String key = PRODUCT_PREFIX + productId;
        return redisHashOperations.get(key, field, Object.class);
    }

    /**
     * Get all product details.
     */
    public Map<String, Object> getProduct(String productId) {
        String key = PRODUCT_PREFIX + productId;
        return redisHashOperations.entries(key, Object.class);
    }

    /**
     * Update a single product field.
     */
    public void updateProductField(String productId, String field, Object value) {
        String key = PRODUCT_PREFIX + productId;
        redisHashOperations.put(key, field, value);
        log.info("Updated product {} field: {} = {}", productId, field, value);
    }

    /**
     * Increment product inventory.
     */
    public long addInventory(String productId, long quantity) {
        String inventoryCount = redisHashOperations.get(INVENTORY_KEY, productId, String.class);
        long currentInventory = inventoryCount != null ? Long.parseLong(inventoryCount) : 0;
        long newInventory = currentInventory + quantity;

        redisHashOperations.put(INVENTORY_KEY, productId, String.valueOf(newInventory));
        log.info("Product {} inventory: {} -> {}", productId, currentInventory, newInventory);
        return newInventory;
    }

    /**
     * Decrement product inventory (for purchase).
     */
    public long decrementInventory(String productId, long quantity) {
        return addInventory(productId, -quantity);
    }

    /**
     * Get current inventory for a product.
     */
    public long getInventory(String productId) {
        String inventoryCount = redisHashOperations.get(INVENTORY_KEY, productId, String.class);
        return inventoryCount != null ? Long.parseLong(inventoryCount) : 0;
    }

    /**
     * Check if product exists.
     */
    public boolean productExists(String productId) {
        String key = PRODUCT_PREFIX + productId;
        return redisHashOperations.hasKey(key, "id");
    }

    /**
     * Delete a product.
     */
    public void deleteProduct(String productId) {
        String key = PRODUCT_PREFIX + productId;
        redisHashOperations.delete(key);
        log.info("Deleted product: {}", productId);
    }

    /**
     * Get all inventory counts.
     */
    public Map<String, Object> getAllInventory() {
        return redisHashOperations.entries(INVENTORY_KEY, Object.class);
    }
}

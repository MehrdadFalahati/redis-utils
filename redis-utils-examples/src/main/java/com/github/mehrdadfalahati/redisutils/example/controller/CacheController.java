package com.github.mehrdadfalahati.redisutils.example.controller;

import com.github.mehrdadfalahati.redisutils.example.model.User;
import com.github.mehrdadfalahati.redisutils.example.service.UserService;
import com.github.mehrdadfalahati.redisutils.example.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller demonstrating Redis operations through HTTP endpoints.
 *
 * <p>Example endpoints:
 * <ul>
 *   <li>POST /api/cache/users - Cache a user</li>
 *   <li>GET /api/cache/users/{userId} - Get cached user</li>
 *   <li>DELETE /api/cache/users/{userId} - Delete cached user</li>
 *   <li>POST /api/cache/products - Save product</li>
 *   <li>GET /api/cache/products/{productId} - Get product</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final UserService userService;
    private final ProductService productService;

    // ========== User Operations ==========

    @PostMapping("/users")
    public ResponseEntity<String> cacheUser(@RequestBody User user) {
        userService.cacheUser(user);
        return ResponseEntity.ok("User cached: " + user.getId());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable String userId) {
        return userService.getCachedUser(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        userService.deleteCachedUser(userId);
        return ResponseEntity.ok("User deleted from cache: " + userId);
    }

    @PostMapping("/users/{userId}/login")
    public ResponseEntity<Long> incrementLogin(@PathVariable String userId) {
        long count = userService.incrementLoginCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/users/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
        boolean exists = userService.isUserCached(userId);
        return ResponseEntity.ok(exists);
    }

    // ========== Product Operations ==========

    @PostMapping("/products")
    public ResponseEntity<String> saveProduct(
        @RequestParam String productId,
        @RequestBody Map<String, Object> productData
    ) {
        productService.saveProduct(productId, productData);
        return ResponseEntity.ok("Product saved: " + productId);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable String productId) {
        Map<String, Object> product = productService.getProduct(productId);
        if (product.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @PutMapping("/products/{productId}/fields/{field}")
    public ResponseEntity<String> updateProductField(
        @PathVariable String productId,
        @PathVariable String field,
        @RequestBody Object value
    ) {
        productService.updateProductField(productId, field, value);
        return ResponseEntity.ok("Product field updated");
    }

    @PostMapping("/products/{productId}/inventory")
    public ResponseEntity<Long> addInventory(
        @PathVariable String productId,
        @RequestParam long quantity
    ) {
        long newInventory = productService.addInventory(productId, quantity);
        return ResponseEntity.ok(newInventory);
    }

    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<Long> getInventory(@PathVariable String productId) {
        long inventory = productService.getInventory(productId);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getAllInventory() {
        return ResponseEntity.ok(productService.getAllInventory());
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product deleted: " + productId);
    }
}


package com.gtcafe.asimov.product;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Product> redisTemplate;

    public ProductService(ProductRepository productRepository, RedisTemplate<String, Product> redisTemplate) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
    }

    private static final String CACHE_KEY_PREFIX = "product:";

    public Product getProduct(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        // 查詢快取
        Product product = redisTemplate.opsForValue().get(cacheKey);
        if (product != null) return product;

        // 查詢資料庫
        product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // 更新快取
            redisTemplate.opsForValue().set(cacheKey, product);
        }
        return product;
    }

    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        // 同步快取
        redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + savedProduct.getId(), savedProduct);
        return savedProduct;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        // 刪除快取
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
    }
}

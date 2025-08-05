package com.example.cacheaside;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CacheService cacheService;

    public Product getProduct(String id) {
        // 1. Try to get from cache
        Product product = (Product) cacheService.get("product:" + id);
        if (product != null) {
            return product;
        }

        // 2. If cache miss, get from database
        product = productRepository.findById(id).orElse(null);

        // 3. If found in database, set to cache
        if (product != null) {
            cacheService.set("product:" + id, product);
        }

        return product;
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(String id, Product updatedProduct) {
        // 1. Update database
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            productRepository.save(product);

            // 2. Invalidate cache
            cacheService.delete("product:" + id);
            return product;
        } else {
            return null;
        }
    }

    public void deleteProduct(String id) {
        // 1. Delete from database
        productRepository.deleteById(id);

        // 2. Invalidate cache
        cacheService.delete("product:" + id);
    }
}
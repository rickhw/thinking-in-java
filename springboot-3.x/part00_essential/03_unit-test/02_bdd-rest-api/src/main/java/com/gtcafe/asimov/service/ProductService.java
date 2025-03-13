package com.gtcafe.asimov.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.gtcafe.asimov.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.gtcafe.asimov.model.Product;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Transactional
    public Product createProduct(Product product) {
        // 添加額外的驗證邏輯
        validateProduct(product);
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product existingProduct = getProductById(id);
        
        existingProduct.setName(productDetails.getName());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setCategory(productDetails.getCategory());
        
        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // 新增用於 BDD 測試的方法
    @Transactional
    public void deleteAll() {
        productRepository.deleteAll();
    }

    // 額外的驗證邏輯
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("產品名稱不能為空");
        }
        
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("價格必須大於0");
        }
        
        if (product.getCategory() == null || product.getCategory().isEmpty()) {
            throw new IllegalArgumentException("類別不能為空");
        }
    }
}
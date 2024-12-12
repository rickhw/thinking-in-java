package com.gtcafe.asimov.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.asimov.model.Product;
import com.gtcafe.asimov.service.ProductService;

@WebMvcTest(ProductController.class)
public class ProductControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private String createProductPayload;
    private String createProductExpected;

    @BeforeEach
    void setUp() throws Exception {
        // 載入 JSON 測試資源
        createProductPayload = new String(Files.readAllBytes(
            Paths.get("src/test/resources/test-data/01-create-product.json")));
        createProductExpected = new String(Files.readAllBytes(
            Paths.get("src/test/resources/test-data/01-expect-result.json")));
    }

    @Test
    @DisplayName("創建產品 - 成功場景")
    void testCreateProduct_Success() throws Exception {
        // 準備模擬服務返回的產品
        Product savedProduct = objectMapper.readValue(createProductExpected, Product.class);
        
        // 模擬服務方法的行為
        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createProductPayload)
                // 添加自定義 Header
                .header("X-Request-Source", "unit-test")
                // 添加 Query String
                .queryParam("validate", "true"))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Wireless Headphones"))
            .andExpect(jsonPath("$.price").value(199.99))
            .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @DisplayName("創建產品 - 驗證失敗的 Payload")
    void testCreateProduct_InvalidPayload() throws Exception {
        // 無效的 Payload
        String invalidPayload = """
        {
            "name": "",
            "price": -10,
            "category": ""
        }
        """;

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.price").exists())
            .andExpect(jsonPath("$.category").exists());
    }

    @Test
    @DisplayName("獲取單一產品 - 成功場景")
    void testGetProduct_Success() throws Exception {
        Product product = Product.builder()
            .id(1L)
            .name("Wireless Headphones")
            .price(199.99)
            .category("Electronics")
            .build();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1")
                .header("Accept-Language", "zh-TW"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Wireless Headphones"));
    }

    @Test
    @DisplayName("更新產品 - 成功場景")
    void testUpdateProduct_Success() throws Exception {
        // 更新的 Payload
        String updatePayload = """
        {
            "name": "Premium Wireless Headphones",
            "price": 249.99,
            "category": "Electronics"
        }
        """;

        Product updatedProduct = Product.builder()
            .id(1L)
            .name("Premium Wireless Headphones")
            .price(249.99)
            .category("Electronics")
            .build();

        when(productService.updateProduct(any(Long.class), any(Product.class)))
            .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload)
                // 添加自定義 Header
                .header("X-Update-Source", "admin-panel"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("Premium Wireless Headphones"))
            .andExpect(jsonPath("$.price").value(249.99));
    }

    @Test
    @DisplayName("刪除產品 - 成功場景")
    void testDeleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/api/products/1")
                .header("X-Delete-Reason", "obsolete"))
            .andExpect(status().isNoContent());
    }
}
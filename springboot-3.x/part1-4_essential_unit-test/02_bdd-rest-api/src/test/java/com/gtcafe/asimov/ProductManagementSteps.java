package com.gtcafe.asimov;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.gtcafe.asimov.model.Product;
import com.gtcafe.asimov.service.ProductService;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest
public class ProductManagementSteps {

    @Autowired
    private ProductService productService;

    private Product currentProduct;
    private List<Product> productList;
    private Long deletedProductId;

    @Before
    public void setup() {
        // 初始化測試數據
        productService.deleteAll(); // 假設添加此方法清空數據
        
        // 預置產品
        Product mouse = Product.builder()
            .id(1L)
            .name("Wireless Mouse")
            .price(29.99)
            .category("Electronics")
            .build();
        
        Product keyboard = Product.builder()
            .id(2L)
            .name("Mechanical Keyboard")
            .price(89.99)
            .category("Electronics")
            .build();
        
        productService.createProduct(mouse);
        productService.createProduct(keyboard);
    }

    @Given("系統中已存在以下產品")
    public void system_has_existing_products(List<Map<String, String>> products) {
        productList = productService.getAllProducts();
        assertEquals(2, productList.size());
    }

    @When("我創建一個新產品")
    public void create_new_product(List<Map<String, String>> productData) {
        Map<String, String> data = productData.get(0);
        Product newProduct = Product.builder()
            .name(data.get("name"))
            .price(Double.parseDouble(data.get("price")))
            .category(data.get("category"))
            .build();
        
        currentProduct = productService.createProduct(newProduct);
    }

    @Then("系統應該返回創建的產品詳情")
    public void verify_product_creation() {
        assertNotNull(currentProduct);
        assertNotNull(currentProduct.getId());
    }

    @Then("產品列表中應包含新創建的產品")
    public void verify_product_in_list() {
        List<Product> updatedList = productService.getAllProducts();
        assertTrue(updatedList.stream()
            .anyMatch(p -> p.getName().equals(currentProduct.getName())));
    }

    @When("我更新 ID 為 {int} 的產品")
    public void update_product(int id, List<Map<String, String>> productData) {
        Map<String, String> data = productData.get(0);
        Product existingProduct = productService.getProductById((long) id);
        
        existingProduct.setName(data.get("name"));
        existingProduct.setPrice(Double.parseDouble(data.get("price")));
        
        currentProduct = productService.updateProduct((long) id, existingProduct);
    }

    @Then("系統應該返回更新後的產品詳情")
    public void verify_product_update() {
        assertNotNull(currentProduct);
        assertEquals("Ergonomic Mouse", currentProduct.getName());
    }

    @Then("產品名稱應該被成功更新")
    public void confirm_product_name_update() {
        Product updatedProduct = productService.getProductById(currentProduct.getId());
        assertEquals("Ergonomic Mouse", updatedProduct.getName());
    }

    @When("我刪除 ID 為 {int} 的產品")
    public void delete_product(int id) {
        deletedProductId = (long) id;
        productService.deleteProduct(deletedProductId);
    }

    @Then("系統應該成功刪除該產品")
    public void verify_product_deletion() {
        assertThrows(Exception.class, () -> {
            productService.getProductById(deletedProductId);
        });
    }

    @Then("產品列表中不應包含已刪除的產品")
    public void confirm_product_removed_from_list() {
        List<Product> remainingProducts = productService.getAllProducts();
        assertFalse(remainingProducts.stream()
            .anyMatch(p -> p.getId().equals(deletedProductId)));
    }

    @When("我嘗試創建一個包含無效信息的產品")
    public void create_invalid_product(List<Map<String, String>> productData) {
        Map<String, String> data = productData.get(0);
        Product invalidProduct = Product.builder()
            .name(data.get("name"))
            .price(data.get("price") == null ? null : Double.parseDouble(data.get("price")))
            .category(data.get("category"))
            .build();
        
        try {
            currentProduct = productService.createProduct(invalidProduct);
            fail("應該拋出驗證異常");
        } catch (Exception e) {
            // 預期會拋出異常
        }
    }

    @Then("系統應該拒絕創建並返回驗證錯誤")
    public void verify_validation_error() {
        assertNull(currentProduct);
    }
}
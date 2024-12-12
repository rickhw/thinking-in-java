我將提供一個使用 Cucumber 進行 BDD (行為驅動開發) 的完整範例。

1. 首先，更新 `build.gradle` 添加 Cucumber 依賴：

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.1'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

repositories {
    mavenCentral()
}

dependencies {
    // 之前的 Spring Boot 依賴
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // Cucumber 依賴
    testImplementation 'io.cucumber:cucumber-java:7.14.0'
    testImplementation 'io.cucumber:cucumber-spring:7.14.0'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.14.0'
    
    // Spring Boot Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    
    // REST Assured (用於 API 測試)
    testImplementation 'io.rest-assured:rest-assured:5.3.2'
    
    // H2 Database
    runtimeOnly 'com.h2database:h2'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
}

// Cucumber 測試配置
test {
    useJUnitPlatform {
        includeTags 'unit-test'
    }
}

// Cucumber 特定任務
task cucumber(type: Test) {
    useJUnitPlatform {
        includeTags 'bdd'
    }
}

```

2. Cucumber 特徵文件 `product-management.feature`:

```gherkin
@bdd
Feature: 產品管理系統
  作為一個產品經理
  我希望能夠管理產品目錄
  以便追蹤和維護產品信息

  Background: 
    Given 系統中已存在以下產品
      | id | name             | price | category     |
      | 1  | Wireless Mouse   | 29.99 | Electronics  |
      | 2  | Mechanical Keyboard | 89.99 | Electronics |

  Scenario: 成功創建新產品
    When 我創建一個新產品
      | name             | price | category     |
      | Smart Watch      | 199.99| Electronics  |
    Then 系統應該返回創建的產品詳情
    And 產品列表中應包含新創建的產品

  Scenario: 更新現有產品
    When 我更新 ID 為 1 的產品
      | name             | price | category     |
      | Ergonomic Mouse  | 39.99 | Electronics  |
    Then 系統應該返回更新後的產品詳情
    And 產品名稱應該被成功更新

  Scenario: 刪除產品
    When 我刪除 ID 為 2 的產品
    Then 系統應該成功刪除該產品
    And 產品列表中不應包含已刪除的產品

  Scenario Outline: 驗證產品創建的輸入驗證
    When 我嘗試創建一個包含無效信息的產品
      | name   | price   | category   |
      | <name> | <price> | <category> |
    Then 系統應該拒絕創建並返回驗證錯誤

    Examples:
      | name              | price  | category     |
      |                   | 199.99 | Electronics  |
      | Smart Watch       | -10    | Electronics  |
      | Valid Product     | 100    |              |

```

3. Cucumber Step Definitions:

```java
package com.example.productapp.bdd;

import com.example.productapp.model.Product;
import com.example.productapp.service.ProductService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

```

4. Cucumber 運行器：

```java
package com.example.productapp.bdd;

import org.junit.platform.suite.api.*;
import io.cucumber.spring.CucumberSpringSupport;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = "cucumber.plugin", 
    value = "pretty,html:build/cucumber-report.html,json:build/cucumber-report.json"
)
public class CucumberRunnerTest extends CucumberSpringSupport {
}

```

5. 修改 `ProductService` 以支持 BDD 測試：

```java
package com.example.productapp.service;

import com.example.productapp.model.Product;
import com.example.productapp.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

```

執行 BDD 測試：

1. 命令行執行
```bash
# 執行 Cucumber 測試
./gradlew cucumber

# 產生報告
./gradlew cucumber testReport
```

2. 測試報告位置
```
build/cucumber-report.html
build/cucumber-report.json
```

BDD 測試特點：
- 使用自然語言描述測試場景
- 覆蓋多種業務邏輯和驗證場景
- 易於理解和維護
- 支持參數化測試

建議：
- 將特徵文件與開發需求對齊
- 保持步驟定義的通用性
- 添加更多邊界條件測試

需要我進一步解釋或提供更多細節嗎？
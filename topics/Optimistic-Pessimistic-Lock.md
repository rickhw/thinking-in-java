以下是 **樂觀鎖（Optimistic Lock）**與**悲觀鎖（Pessimistic Lock）** 的整理，包含概念背景、適用場景、差異比較，並附上 Java 範例：

---

## 🔒 樂觀鎖 vs 悲觀鎖 概念整理

| 項目       | 樂觀鎖（Optimistic Lock）    | 悲觀鎖（Pessimistic Lock）             |
| -------- | ----------------------- | --------------------------------- |
| **概念**   | 假設不會發生衝突，操作時不加鎖，提交時檢查版本 | 假設會發生衝突，操作前即加鎖以阻止並發修改             |
| **技術依據** | 通常透過「版本號」或「時間戳」來控制並發寫入  | 直接使用資料庫鎖機制（如 `SELECT FOR UPDATE`） |
| **性能**   | 讀多寫少效能佳，因避免加鎖成本         | 讀寫都需加鎖，效能較差                       |
| **鎖粒度**  | 邏輯控制，應用層或 ORM 實作        | 資料庫層級的實體鎖                         |
| **適用場景** | **讀多寫少**、資源衝突機率低        | **寫多寫多** 或強一致性需求的場景               |

---

## ✅ 樂觀鎖 Java 範例（JPA 版）

假設你使用的是 Spring Boot + JPA，使用 `@Version` 標記實作：

```java
import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    private Long id;

    private String name;

    private int stock;

    @Version  // 樂觀鎖機制的版本欄位
    private int version;

    // getters/setters
}
```

更新邏輯（由 JPA 自動幫你檢查 version）：

```java
@Transactional
public void decreaseStock(Long productId) {
    Product product = productRepository.findById(productId).orElseThrow();
    product.setStock(product.getStock() - 1);
    // 儲存時，若 version 不一致，會拋出 OptimisticLockException
}
```

❗ 若兩個交易同時讀到 version = 1，提交時只有一個能成功，另一個會拋 `OptimisticLockException`。

---

## 🔒 悲觀鎖 Java 範例（JPA + SELECT FOR UPDATE）

透過原生 SQL 或 JPA 的悲觀鎖模式：

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Product findByIdForUpdate(@Param("id") Long id);
```

用法：

```java
@Transactional
public void decreaseStockPessimistic(Long productId) {
    Product product = productRepository.findByIdForUpdate(productId);
    product.setStock(product.getStock() - 1);
    // 悲觀鎖會鎖住該 row，直到交易提交
}
```

⚠️ 此方式會在查詢時對資料加上 **排他鎖（EXCLUSIVE LOCK）**，其他交易需等待。

---

## 🧠 總結建議

| 場景          | 建議鎖模式      |
| ----------- | ---------- |
| 電商搶購、高競爭更新  | 悲觀鎖        |
| 一般表單更新、讀多寫少 | 樂觀鎖        |
| 大量批次非同步處理   | 樂觀鎖 + 重試機制 |

---

如你有特定資料庫（MySQL、PostgreSQL）或框架（Spring Data JDBC、MyBatis）等環境，也可以幫你給對應範例。需要嗎？

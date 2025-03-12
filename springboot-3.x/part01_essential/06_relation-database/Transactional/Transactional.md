

在你的程式碼中，`@Transactional` 是 Spring 提供的一個註解，用來管理資料庫交易 (transaction) 的範圍與行為。以下是詳細的解釋、用法和注意事項：

---

### **@Transactional 的作用**
`@Transactional` 的主要作用是管理資料庫操作的交易性，確保多個操作要麼全部成功 (commit)，要麼全部失敗 (rollback)，以維護資料的一致性和完整性。

在你的程式碼中，`@Transactional` 用在 `createProduct`, `updateProduct`, 和 `deleteProduct` 方法上，涵蓋了對資料庫進行修改的操作。這些方法的執行流程如下：
1. **交易開始 (Transaction Start)**：當方法被調用時，Spring 開始一個交易。
2. **執行業務邏輯 (Execute Logic)**：方法內執行的所有資料庫操作，都在該交易範圍內。
3. **提交 (Commit)**：如果方法成功執行完畢，則提交交易。
4. **回滾 (Rollback)**：如果方法中拋出未捕獲的 `RuntimeException` 或 `Error`，則回滾交易，取消對資料庫的變更。

---

### **用法說明**
1. **方法級別的應用**
   - 你將 `@Transactional` 標記在方法上，表示該方法的所有資料庫操作都包含在一個交易中。
   - 適合於單一操作或彼此依賴的多個操作。

2. **類級別的應用**
   - 如果在類上標註 `@Transactional`，則該類的所有方法默認都會被交易管理。
   - 適合於需要交易管理的大量操作的類別。

---

### **程式碼中的應用範例**
1. **`createProduct(Product product)`**
   - 交易開始後，將新產品儲存到資料庫。
   - 如果儲存過程中出現異常，例如違反資料庫約束，則回滾交易。

2. **`updateProduct(Long id, Product productDetails)`**
   - 查詢指定 ID 的產品。
   - 更新產品的屬性。
   - 如果查詢過程或更新過程中有異常，則回滾交易。

3. **`deleteProduct(Long id)`**
   - 查詢指定 ID 的產品。
   - 刪除產品。
   - 如果刪除過程中有異常，例如產品不存在，則回滾交易。

---

### **注意事項**
1. **默認行為**
   - Spring 預設只有在方法中拋出未捕獲的 `RuntimeException` 或 `Error` 時，才會觸發交易回滾。對於檢查型異常 (`Checked Exception`)，如 `IOException`，則不會自動回滾。
   - 如果希望對特定檢查型異常回滾，可以使用 `@Transactional(rollbackFor = Exception.class)`。

   ```java
   @Transactional(rollbackFor = {IOException.class})
   public void someMethod() throws IOException {
       // 業務邏輯
   }
   ```

2. **非公開方法不適用**
   - Spring 的 AOP 機制無法攔截同類別內部的非公開方法或直接呼叫的私有方法，因此 `@Transactional` 不會生效。例如：
     ```java
     @Transactional
     public void publicMethod() {
         privateMethod(); // 私有方法的交易管理無法啟用
     }

     private void privateMethod() {
         // 無法被 @Transactional 管理
     }
     ```

3. **與查詢操作分離**
   - 一般來說，只讀操作（如 `getAllProducts` 和 `getProductById`）不需要標註 `@Transactional`，以避免額外的交易開銷。如果必要，可以設定只讀屬性：
     ```java
     @Transactional(readOnly = true)
     public List<Product> getAllProducts() {
         return productRepository.findAll();
     }
     ```

4. **懶加載 (Lazy Loading)**
   - 如果方法在交易結束後訪問懶加載的資料（如延遲初始化的關聯屬性），可能會拋出 `LazyInitializationException`。解決方法包括在交易內完成資料的訪問或設定 FetchType 為 `EAGER`。

5. **資料庫鎖與性能**
   - 在大交易範圍內，可能會增加資料庫鎖的持續時間，導致性能問題。需要合理設計交易範圍，避免長時間佔用鎖資源。

---

### **總結**
`@Transactional` 是管理資料庫交易的強大工具，可以確保資料的一致性與完整性。在使用時需注意交易範圍的設定、例外處理與性能影響，以避免潛在問題。

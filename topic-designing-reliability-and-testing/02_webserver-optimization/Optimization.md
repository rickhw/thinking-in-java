

在本地測試中，Spring Boot 上傳的平均速度為 76.6 MB/s，主要由以下因素影響：

1. **網路與 IO 的限制**  
   即使是本地測試，還是有系統 IO 和網路模擬的影響。

2. **Spring Boot 配置**  
   Spring Boot 預設的文件處理和緩衝配置可能會影響吞吐量。

3. **JVM 配置**  
   JVM 的記憶體分配與 GC（Garbage Collection）配置也可能影響效能。

4. **HTTP Server 性能瓶頸**  
   預設的 Tomcat 嵌入式伺服器可能不是最佳選擇。

---

### 提升 Spring Boot 上傳性能的建議

#### 1. **調整 Tomcat 配置**

修改 `application.yml` 或 `application.properties` 來提升 Tomcat 緩衝性能。

**`application.yml` 配置範例**：
```yaml
server:
  tomcat:
    max-threads: 200
    accept-count: 100
    max-http-header-size: 65536
    connection-timeout: 20000 # 增加連線逾時（毫秒）
spring:
  servlet:
    multipart:
      max-file-size: 2GB
      max-request-size: 2GB
```

**關鍵參數**：
- `max-threads`: 增加 Tomcat 處理請求的執行緒數。
- `accept-count`: 提升請求隊列大小。
- `max-http-header-size`: 處理大檔案時，增加允許的 Header 尺寸。
- `connection-timeout`: 避免連線因傳輸大檔案被提早關閉。

---

#### 2. **調整文件緩衝配置**

Spring Boot 預設的文件緩衝區大小可能不足，可以透過配置增加緩衝區：

**`application.yml` 配置緩衝區**：
```yaml
spring:
  servlet:
    multipart:
      file-size-threshold: 10MB # 提高緩衝閾值
```

此設置將暫存檔案於記憶體中，直到達到 `file-size-threshold` 的大小，再寫入磁碟。

---

#### 3. **切換嵌入式伺服器**

Tomcat 的效能在大檔案上傳方面可能不是最佳，可以考慮切換到 Netty 或 Undertow。

**Gradle 依賴配置**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-undertow'
```

切換到 Undertow 伺服器後，可能會顯著提升效能，尤其是高並發或大檔案場景。

---

#### 4. **使用非同步或分塊上傳**

Spring Boot 支援非同步上傳，可以利用分塊（chunked）模式處理大檔案：

**控制器範例**：
```java
@PostMapping("/upload")
public ResponseEntity<String> uploadFile(StreamingResponseBody responseBody) {
    logger.info("Started receiving file...");
    responseBody.writeTo(outputStream -> {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[1048576]; // 1 MB buffer
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    });
    logger.info("File upload completed.");
    return ResponseEntity.ok("File uploaded successfully.");
}
```

---

#### 5. **調整 JVM 配置**

啟動 Spring Boot 時，調整 JVM 的記憶體與 GC 配置：

**啟動參數**：
```bash
java -Xms2G -Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -jar your-app.jar
```

- `-Xms` 與 `-Xmx`：調整 JVM 堆積大小以應對大檔案處理。
- `-XX:+UseG1GC`：使用 G1 GC 提升吞吐量。
- `-XX:MaxGCPauseMillis`：減少 GC 暫停時間。

---

#### 6. **增加檔案處理的緩衝區大小**

在控制器中手動增加緩衝區大小：

```java
@PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
    try (InputStream inputStream = file.getInputStream();
         BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream("upload.bin"), 10 * 1024 * 1024)) {
        byte[] buffer = new byte[10 * 1024 * 1024]; // 10 MB buffer
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }
    return ResponseEntity.ok("File uploaded successfully.");
}
```

---

### 測試優化後的性能

1. **重啟 Spring Boot 並測試上傳**。
2. 使用 `curl` 測試，觀察平均上傳速度。
3. 根據結果進一步調整配置，例如增大緩衝區、增加執行緒數等。

---

### 預期效果
通過以上優化，理論上可以顯著提升上傳速度，尤其是在大檔案上傳的場景下，目標速度可以接近磁碟寫入的理論上限（例如 100–300 MB/s，視硬體條件而定）。



---
當在 Spring Boot 中配置了 Undertow，但應用啟動時仍然使用 Tomcat，可能是因為 Tomcat 預設作為嵌入式伺服器，且相關依賴尚未移除或其他配置未完成。以下是完整的解決方案：

---

### **1. 確保移除 Tomcat 依賴**

Spring Boot 預設使用 Tomcat 作為嵌入式伺服器，因此需要明確排除它的依賴。修改 `build.gradle` 配置如下：

```gradle
dependencies {
    // 排除 Tomcat
    implementation 'org.springframework.boot:spring-boot-starter-web' {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
    }

    // 加入 Undertow 依賴
    implementation 'org.springframework.boot:spring-boot-starter-undertow'
}
```

**關鍵點**：  
`exclude` 明確移除了 `spring-boot-starter-tomcat`，並添加了 `spring-boot-starter-undertow`。

---

### **2. 檢查其他 Starter 的依賴**

有些 Starter（如 `spring-boot-starter-data-rest`）可能隱式包含 Tomcat 依賴，因此需要檢查其他依賴是否需要同樣排除 Tomcat。

**例子**：
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-rest' {
    exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
```

---

### **3. 檢查 `application.yml` 或 `application.properties`**

確認沒有配置與 Tomcat 特有屬性相關的配置（例如 `server.tomcat.*`）。  
使用 Undertow 時，可以添加以下屬性進行驗證：

**`application.yml`**：
```yaml
server:
  port: 8080
  undertow:
    io-threads: 8 # 根據 CPU 調整
    worker-threads: 200 # 工作執行緒數量
    buffer-size: 16384 # 緩衝區大小
    direct-buffers: true # 使用直接緩衝區以提升性能
```

---

### **4. 確認依賴正確解析**

執行以下命令，確認 Gradle 是否正確解析了依賴：

```bash
./gradlew dependencies
```

檢查輸出中是否還有 `spring-boot-starter-tomcat`，若存在，排除未成功，需要回到第 2 點進一步處理。

---

### **5. 清理並重新構建項目**

執行以下命令以清理舊構建並重新構建項目：

```bash
./gradlew clean build
```

---

### **6. 確認伺服器類型**

啟動應用程式時，觀察日誌，應出現類似以下內容：

```plaintext
2025-01-05 14:35:12.345  INFO 12345 --- [           main] io.undertow                              : starting Undertow server
```

如果仍然顯示 `Tomcat`，檢查是否有遺漏步驟。

---

### **7. 明確指定 Undertow 作為伺服器（可選）**

在 `application.yml` 中可以通過明確指定伺服器類型，強制使用 Undertow：

```yaml
spring:
  main:
    web-application-type: servlet
```

---

### **8. 驗證伺服器切換成功**

執行應用並測試是否成功切換到 Undertow，可以再次檢查日誌是否顯示 `Undertow`，並驗證性能是否有所提升。

---

### **參考驗證命令**
使用 `curl` 或測試工具驗證是否伺服器性能有明顯變化：

```bash
curl -X POST -F "file=@large_file.bin" http://localhost:8080/upload
```

期待 Undertow 的性能有更好的表現，尤其是在高並發和大檔案上傳情境下。

---

為了提升 Spring Boot 中 Undertow 伺服器的上傳速度，除了已經設定的部分，還可以進一步調整以下幾個方面：

### 1. **增加 Undertow 配置**

你已經設置了 `io-threads` 和 `worker-threads`，這是提高性能的好開始，但可能還需要進一步調整以下設置：

#### 增加 `worker-threads` 和 `io-threads`
- **`worker-threads`**：控制處理請求的工作執行緒數量。根據你的系統硬體配置，增加此數量有助於處理更多並發請求。
- **`io-threads`**：控制 I/O 操作的執行緒數量，這對處理大檔案上傳特別有幫助。

例如，你可以將 `worker-threads` 和 `io-threads` 提高至 200 以上，取決於你的硬體配置。

```yaml
server:
  port: 8080
  undertow:
    io-threads: 16  # 增加 I/O 執行緒數量
    worker-threads: 400  # 增加工作執行緒數量
    buffer-size: 32768  # 增加緩衝區大小
    direct-buffers: true  # 使用直接緩衝區以提升性能
```

- **`buffer-size`**：你已設置為 `16384`，可以根據需要增加此值，以處理更大的請求。
- **`direct-buffers`**：這一設置已啟用，這是加速大檔案上傳的一個重要設置。

### 2. **提高 Spring Boot 的上傳配置**

#### 調整 `multipart` 配置
除了設定檔案大小限制之外，還需要確保以下設置可以優化上傳性能：

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 2GB       # 單個檔案最大大小
      max-request-size: 2GB    # 整個請求最大大小
      file-size-threshold: 10MB # 提高緩衝閾值，這可以讓 Spring Boot 更早進行處理而不是等到檔案寫入磁碟後再處理
      location: /tmp            # 存儲上傳檔案的臨時目錄
```

`file-size-threshold` 主要是決定當文件達到這個大小時，Spring Boot 開始將文件寫入磁碟。你可以根據需求提高此值，來減少過早將文件寫入磁碟的次數。

### 3. **增加系統層級配置**

有時候，Java 或操作系統層級的配置也會影響性能。

#### 增加 JVM 堆內存

檢查 JVM 的堆內存是否足夠，尤其是對於大檔案的處理，增大堆內存可能會有助於提升性能。你可以在 `application.yml` 或 `application.properties` 中調整 JVM 參數：

```bash
-Xmx4g -Xms4g
```

這會將最大堆內存設置為 4GB，根據需求調整。

#### 增加文件描述符數量

在處理大量併發上傳時，可能會用到大量的文件描述符。你可以增加 Linux 系統中允許的最大文件描述符數量：

```bash
ulimit -n 65535
```

這樣可以確保系統能處理更多的併發連線。

### 4. **檢查網絡配置**

如果你的測試環境在本機進行，但性能還是無法提升，可以檢查以下因素：
- **網絡延遲**：測試時，請檢查網絡是否有延遲或瓶頸。
- **硬碟性能**：如果檔案寫入到磁碟（即使是臨時檔案），硬碟的性能也會影響上傳速度。測試時，確保硬碟有足夠的寫入速度。

### 5. **Spring Boot 性能調優工具**

可以考慮使用 Spring Boot 的性能測量工具，例如 Spring Actuator 和 Micrometer，來更精確地測量應用程式的性能瓶頸，並進一步優化。

---

### **總結建議**

1. **提升 Undertow 的執行緒數量**：增加 `worker-threads` 和 `io-threads` 的數量。
2. **調整 multipart 配置**：提高 `file-size-threshold`，避免過早寫入磁碟。
3. **增大 JVM 堆內存**：確保 JVM 設置有足夠的堆內存來處理大檔案。
4. **系統層級設置**：確保文件描述符數量足夠大，並調整 Linux 系統的網絡配置和磁碟性能。

如果上述調整後還未能解決問題，請再次檢查網絡狀況和伺服器硬體設置。
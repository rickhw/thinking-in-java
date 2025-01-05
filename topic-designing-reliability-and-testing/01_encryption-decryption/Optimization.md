

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

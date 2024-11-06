## Prompt

幫我用 java17, 寫一個 發送 http request 的 client 模擬程式，有以下幾個需求：

1. 指定單位時間內發送的次數，單位可以指定秒、分，次數預設 10 次
2. 發送請求，單位時間內的發送是平均發送的，例如每秒 5 次，代表平均 0.2s 發送一次


---
## ChatGPT

這是一個使用 Java 17 寫的簡單 HTTP Client 程式，可以模擬根據設定的單位時間（秒或分鐘）和次數來發送 HTTP 請求。這個程式可以根據給定的次數均勻分配請求，並適應不同的單位時間。

### 程式碼範例

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpClientSimulator {
    private static final int DEFAULT_RATE = 10;
    private static final TimeUnit DEFAULT_UNIT = TimeUnit.SECONDS;

    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;

    public HttpClientSimulator() {
        this.httpClient = HttpClient.newHttpClient();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startSendingRequests(String url, int rate, TimeUnit unit) {
        int interval = calculateInterval(rate, unit);
        System.out.println("Starting to send requests to " + url + " every " + interval + " ms.");

        scheduler.scheduleAtFixedRate(() -> sendRequest(url), 0, interval, TimeUnit.MILLISECONDS);
    }

    private int calculateInterval(int rate, TimeUnit unit) {
        if (unit == TimeUnit.SECONDS) {
            return 1000 / rate;  // 每秒的間隔毫秒數
        } else if (unit == TimeUnit.MINUTES) {
            return (60 * 1000) / rate;  // 每分鐘的間隔毫秒數
        } else {
            throw new IllegalArgumentException("Unsupported time unit");
        }
    }

    private void sendRequest(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> System.out.println("Received response with status code: " + statusCode))
                .exceptionally(e -> {
                    System.out.println("Request failed: " + e.getMessage());
                    return null;
                });
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("Request sending stopped.");
    }

    public static void main(String[] args) {
        String url = "http://example.com"; // 請替換為目標網址
        int rate = 5;                      // 每秒發送 5 次請求
        TimeUnit unit = TimeUnit.SECONDS;   // 使用秒作為單位

        HttpClientSimulator simulator = new HttpClientSimulator();
        simulator.startSendingRequests(url, rate, unit);

        // 程式運行 10 秒後停止
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        simulator.stop();
    }
}
```

### 說明

1. **發送請求次數**：`rate` 參數決定每單位時間的請求次數，預設值可以設為 `10`。
2. **單位時間間隔計算**：
   - 使用 `calculateInterval` 方法來計算每次請求之間的時間間隔，根據指定的 `TimeUnit` 計算出每次發送的毫秒間隔。
   - 支援秒 (`TimeUnit.SECONDS`) 和分鐘 (`TimeUnit.MINUTES`)。
3. **模擬請求發送**：`ScheduledExecutorService` 用於定期發送請求，按指定間隔將請求平均分佈。
4. **自動停止**：`main` 方法中，程式會運行 10 秒後停止模擬，這樣在測試中可以避免無限運行。

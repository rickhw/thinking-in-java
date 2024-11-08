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
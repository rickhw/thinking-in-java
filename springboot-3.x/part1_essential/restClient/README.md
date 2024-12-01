
## Prompt

我有一個 java17, springboot 3.2, gradle 的應用程式，需要對一個 REST API 送出請求，並且包含 錯誤處理。

請提供完整的程式，滿足以下需求：

1. 這段範例，請用 com.gtcafe 當作 package，起動為 Main.java, 使用 gradle
2. 每個 API 請求:
    - 都透過常用的 Authenthication 的方法, 這些方法請幫我放在 application.yml, 然後幫我設計一個結構
    - 可以帶 HTTP Header / Query String / Payload
3. 每個 API 的 Response 都要處理 HTTP 2XX, 3XX, 4XX, 5XX 等邏輯
4. 每個 API 請求都可以設定 Time out, 這些設定放在 application.yml

請在 application.yml 設計個結構，提供多個 API call 以及相關參數，並提供完整的 java 程式


## ChatGPT

這些錯誤出現是因為 Spring Boot 3.2 使用了新的 `HttpStatusCode` 類型，而不是先前的 `HttpStatus`，以及 `HttpComponentsClientHttpRequestFactory` 的逾時設定方式的不同。以下是解決這些錯誤的方法：

1. **將 `HttpStatusCode` 轉換為 `HttpStatus`**：直接使用 `HttpStatusCode` 或通過 `HttpStatus.valueOf(...)` 將其轉換為 `HttpStatus`。
2. **`setReadTimeout` 方法不存在**：在 `HttpComponentsClientHttpRequestFactory` 上設置逾時參數時，我們可以使用 `RequestConfig`。

### 修改後的程式碼

#### ApiService.java

在 `getStatusCode()` 的結果上使用 `.valueOf(...)` 來將 `HttpStatusCode` 轉換為 `HttpStatus`：

```java
private ResponseEntity<String> handleResponse(ResponseEntity<String> response) {
    HttpStatus status = HttpStatus.valueOf(response.getStatusCode().value());
    if (status.is2xxSuccessful()) {
        System.out.println("Success: " + response.getBody());
    } else if (status.is3xxRedirection()) {
        System.out.println("Redirection: " + response.getHeaders().getLocation());
    } else if (status.is4xxClientError()) {
        System.out.println("Client Error: " + status);
    } else if (status.is5xxServerError()) {
        System.out.println("Server Error: " + status);
    }
    return response;
}
```

#### RestTemplateConfig.java

使用 `RequestConfig` 來設定 `HttpComponentsClientHttpRequestFactory` 的連接和讀取逾時：

```java
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Bean
public RestTemplate restTemplate() {
    RequestConfig config = RequestConfig.custom()
        .setConnectTimeout(connectTimeout)
        .setSocketTimeout(readTimeout)
        .build();

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(
            HttpClients.custom().setDefaultRequestConfig(config).build());

    RestTemplate restTemplate = new RestTemplate(factory);
    restTemplate.setErrorHandler(new CustomErrorHandler());
    return restTemplate;
}
```

#### CustomErrorHandler.java

在 `CustomErrorHandler` 中同樣需要使用 `.valueOf()` 來轉換 `HttpStatusCode`：

```java
@Override
public void handleError(ClientHttpResponse response) throws IOException {
    HttpStatus statusCode = HttpStatus.valueOf(response.getStatusCode().value());
    switch (statusCode.series()) {
        case CLIENT_ERROR:
            System.out.println("4XX Client Error: " + statusCode);
            break;
        case SERVER_ERROR:
            System.out.println("5XX Server Error: " + statusCode);
            break;
        default:
            System.out.println("Unexpected error: " + statusCode);
    }
}
```

這些改動應該能解決您在編譯時遇到的錯誤。
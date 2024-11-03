要在 Spring Boot 應用程式中監控 JVM、CPU、記憶體和物件的使用狀況，並使用 Grafana 進行可視化，通常會用到 Prometheus 和 Micrometer library，然後將 Grafana 設置為讀取 Prometheus 中的指標資料。以下是完整的範例和配置。

### 1. 在 Spring Boot 中配置 Micrometer 和 Prometheus

首先，加入 Micrometer 和 Prometheus 依賴，這樣 Spring Boot 就能自動暴露 Prometheus 指標端點。

**`build.gradle`**:
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
}
```

### 2. 配置 Spring Boot 應用程式的 Actuator

在 `application.yml` 中啟用 Spring Boot Actuator 並暴露必要的指標端點。

**`src/main/resources/application.yml`**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  endpoint:
    prometheus:
      enabled: true
  metrics:
    enable:
      all: true
  health:
    diskspace:
      enabled: true
```

這樣配置後，`/actuator/prometheus` 端點就會暴露應用程式的指標數據。

### 3. 創建 Docker-Compose 文件來啟動 Prometheus 和 Grafana

使用 Docker-Compose 來啟動 Prometheus 和 Grafana。Prometheus 將從 Spring Boot 應用程式中抓取指標，而 Grafana 將從 Prometheus 中讀取指標以生成圖表。

**`docker-compose.yml`**:
```yaml
version: '3'

services:
  springboot-app:
    image: your-spring-boot-app-image # 替換為您應用程式的映像名稱
    ports:
      - "8080:8080"
    networks:
      - monitoring

  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks:
      - monitoring

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin # 設置 Grafana 管理員密碼
    networks:
      - monitoring

networks:
  monitoring:
    driver: bridge
```

### 4. 配置 Prometheus 來抓取 Spring Boot 指標

創建一個 `prometheus.yml` 文件，定義 Prometheus 抓取 Spring Boot 應用程式的頻率和目標。

**`prometheus.yml`**:
```yaml
global:
  scrape_interval: 15s # 每 15 秒抓取一次指標

scrape_configs:
  - job_name: 'springboot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['springboot-app:8080']
```

### 5. 啟動服務

在專案目錄中執行以下命令以啟動 Spring Boot 應用程式、Prometheus 和 Grafana：

```bash
docker-compose up -d
```

### 6. 配置 Grafana 以讀取 Prometheus 指標

進入 [http://localhost:3000](http://localhost:3000)，登入 Grafana（預設帳號：`admin` / 密碼：`admin`），並依照以下步驟設定 Prometheus 作為資料來源：

1. 點擊左側的 **"Configuration"** -> **"Data Sources"**。
2. 點擊 **"Add data source"**，選擇 **"Prometheus"**。
3. 在 **URL** 欄位中輸入 `http://prometheus:9090`，然後點擊 **"Save & Test"** 確認連線正常。

### 7. 在 Grafana 建立儀表板來監控指標

一旦 Prometheus 資料來源設定成功，就可以創建儀表板並新增各種指標（例如 JVM 記憶體、CPU 使用率等）圖表，以可視化 Spring Boot 應用程式的資源使用情況。

此範例配置能讓您透過 Grafana 監控 Spring Boot 應用程式的資源使用狀況，並進行相應的優化或調整。
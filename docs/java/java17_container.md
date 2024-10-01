在使用 Java 17 和 Spring Boot 3.2 開發應用程式時，進行打包、容器化和部署到 Kubernetes（K8s）是常見的步驟。以下是針對這些過程中需要注意的部分整理：

---

## 1. 打包成容器（Dockerfile）時的注意事項

### a. 選擇正確的基礎映像

Java 17 是一個 LTS 版本，並且 Spring Boot 3.2 需要至少 Java 17 的支持，因此在撰寫 Dockerfile 時要選擇合適的基礎映像。
常見的選擇：

OpenJDK 官方映像：
```dockerfile
FROM openjdk:17-jdk-alpine
```

GraalVM 映像（若使用 Native 編譯）：

```dockerfile
FROM ghcr.io/graalvm/native-image:latest
```

注意：使用較輕量的映像（如 alpine）可以減少容器大小，但要確認應用和依賴的兼容性。

### b. 優化 Spring Boot 應用打包方式

分層打包：Spring Boot 提供分層打包支持，讓 Docker 層能夠有效利用緩存，從而加速重建過程。需要將應用的可變內容和不變內容分開處理，減少不必要的重建。

生成分層打包：

```bash
./gradlew bootBuildImage
```

手動配置分層：

```dockerfile
FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

注意：分層打包能提高構建效率，特別是在頻繁更新應用時。

### c. 設定 JVM 選項

內存管理：在容器中運行 Java 應用時，內存分配需進行優化，特別是使用 JVM 的內存選項來控制堆內存大小。
使用 Docker 提供的內存限制來優化 JVM 行為：

```dockerfile
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
```

注意：-XX:+UseContainerSupport 確保 JVM 正確識別容器內的資源限制，從而不會耗盡系統內存。

### d. 壓縮與瘦化容器

使用 jlink 來創建自定義的 Java 運行時，從而減小容器映像大小。這樣可以僅包含應用所需的模塊。

```bash
jlink --module-path $JAVA_HOME/jmods --add-modules java.base --output /custom-jre
```

注意：減少容器大小可以優化部署速度及運行效率，尤其是大規模應用的場景。

---
## 2. 部署到 Kubernetes 的注意事項

### a. 資源配置

CPU 和內存限制：為應用容器設置合理的資源限制與請求，以確保集群資源的有效利用。
YAML 範例：

```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1"
```

注意：合理的資源請求和限制不僅能保護應用免受資源耗盡影響，也能避免過度分配導致的資源浪費。

### b. 健康檢查與重啟策略

使用 livenessProbe 和 readinessProbe 來檢測應用的健康狀況，避免應用進入不健康狀態而無法提供服務。

範例：

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 20
  periodSeconds: 5
```

注意：使用 Spring Boot 提供的 /actuator/health 作為健康檢查端點可以確保 Kubernetes 探測到應用的真實狀態。


### c. 配置管理與機密管理

ConfigMap 和 Secrets：在 Kubernetes 中，應用的配置和機密信息應分別通過 ConfigMap 和 Secrets 管理，並注入容器。

ConfigMap 範例：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  SPRING_DATASOURCE_URL: "jdbc:mysql://db:3306/mydb"
```

Secrets 範例：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  SPRING_DATASOURCE_PASSWORD: cGFzc3dvcmQ=  # base64 encoded password
```

注意：將應用的敏感數據存儲於 Secrets 中並使用環境變數或文件掛載的方式注入到容器中，以提高安全性。

### d. 滾動更新與版本管理

滾動更新：在 Kubernetes 中使用滾動更新策略，避免應用更新時中斷服務。這對於 Spring Boot 應用尤其重要，因為它們需要時間啟動並預熱。

滾動更新配置：

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 1
    maxSurge: 1
```

注意：確保 readinessProbe 正常設置，這樣 K8s 可以在應用準備好後再將其加入流量分發中，避免不健康的實例承擔流量。

### e. 日誌管理

使用 Kubernetes 的日誌管理工具或外部解決方案（如 ELK 堆疊、Prometheus 等）來集中管理 Spring Boot 應用的日誌。
Spring Boot 3.2 支持更加靈活的觀察性工具，可以通過 Actuator 集成 Prometheus 或 OpenTelemetry。

> 注意：確保容器化應用的日誌流向標準輸出（stdout/stderr），便於 K8s 管理並進行集中式日誌分析。

### f. 自動縮放與彈性伸縮

Horizontal Pod Autoscaler (HPA)：Kubernetes 提供了 HPA 來根據應用負載自動擴展或縮減 Pod 數量。可以基於 CPU 或自定義指標進行彈性調整。

HPA 範例：

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: app-deployment
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 75
```

注意：在高流量應用中，HPA 能夠根據實時負載動態調整實例數量，提升系統的彈性和可擴展性。

---

## 總結

- Dockerfile 打包注意事項：選擇正確的基礎映像，進行分層打包、優化內存管理，並可考慮使用 jlink 瘦化容器映像。
- K8s 部署注意事項：需配置合理的資源限制、健康檢查，使用 ConfigMap 和 Secrets 管理配置，並啟用滾動更新和自動擴展策略。此外，應用的日誌和指標應進行集中管理。
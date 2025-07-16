


在設計 **Raft 演算法的 log-based metrics system** 時，我們的目標是透過日誌與監控系統收集重要的 **關鍵指標（Key Metrics）**，用於：

* 可視化 Raft 狀態與健康
* 偵測節點異常（如 Leader flapping、選舉過頻、同步失敗）
* 分析分散式系統的穩定性與一致性進度

---

## ✅ 一、關鍵指標設計（Key Metrics for Raft）

| 分類         | 指標名稱                                           | 說明                                       |
| ---------- | ---------------------------------------------- | ---------------------------------------- |
| **角色狀態**   | `raft_node_role{node="node1"}`                 | 當前節點角色：0=Follower, 1=Candidate, 2=Leader |
| **選舉統計**   | `raft_term_current{node="node1"}`              | 當前任期 (term)                              |
|            | `raft_election_count_total{node="node1"}`      | 節點發起選舉次數（越多代表不穩定）                        |
|            | `raft_vote_granted_total{node="node1"}`        | 該節點給出的投票次數                               |
|            | `raft_vote_received_total{node="node1"}`       | 該節點獲得的投票次數                               |
| **同步進度**   | `raft_log_index_last{node="node1"}`            | 本節點目前 log 最後 index                       |
|            | `raft_commit_index{node="node1"}`              | 本節點目前 commit index                       |
|            | `raft_log_lag{node="node1"}`                   | log 與 Leader 相差的 index（同步延遲）             |
| **RPC 呼叫** | `raft_append_entries_sent_total{to="node2"}`   | Leader 傳送 AppendEntries RPC 次數           |
|            | `raft_append_entries_failed_total{to="node2"}` | AppendEntries RPC 失敗次數                   |
|            | `raft_heartbeat_latency_seconds{to="node2"}`   | heartbeat 延遲時間分佈（直方圖）                    |

---

## ✅ 二、Spring Boot + Micrometer + Prometheus + Grafana 整合

### 🔧 1. `build.gradle` 新增依賴：

```groovy
dependencies {
    implementation 'io.micrometer:micrometer-registry-prometheus'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

---

### 🔧 2. `application.yml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

### 📈 3. 在 `RaftNode.java` 中設置 Metrics

```java
@Autowired
private MeterRegistry meterRegistry;

private Counter electionCount;
private Counter voteGranted;
private AtomicInteger currentTermGauge;
private AtomicInteger currentRoleGauge;

@PostConstruct
public void initMetrics() {
    electionCount = Counter.builder("raft_election_count_total")
        .description("Total number of elections initiated")
        .tag("node", nodeId)
        .register(meterRegistry);

    voteGranted = Counter.builder("raft_vote_granted_total")
        .description("Number of times this node granted a vote")
        .tag("node", nodeId)
        .register(meterRegistry);

    currentTermGauge = meterRegistry.gauge("raft_term_current", Tags.of("node", nodeId), new AtomicInteger(state.currentTerm));
    currentRoleGauge = meterRegistry.gauge("raft_node_role", Tags.of("node", nodeId), new AtomicInteger(state.role.ordinal()));
}
```

### ✅ 當選舉開始時：

```java
electionCount.increment();
state.currentTerm++;
currentTermGauge.set(state.currentTerm);
currentRoleGauge.set(Role.CANDIDATE.ordinal());
```

### ✅ 當接受投票請求：

```java
voteGranted.increment();
```

---

## 🧪 三、啟動 Prometheus + Grafana 監控（簡要）

你可以在 `docker-compose.yml` 中加入 Prometheus + Grafana：

```yaml
services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
```

設定 Prometheus 抓取 Spring Boot 埠口 `/actuator/prometheus`。

---

## 🎯 四、Grafana 觀測重點 Dashboard 建議

* 📊 Raft 角色變化趨勢（折線圖）
* 🔁 每秒 RPC 數量（AppendEntries）
* 🧭 選舉次數與當前 term
* 📉 Log lag (Leader vs Follower)
* 🚨 Alert Rule：

  * 30 秒內選舉超過 3 次 ➜ 發送警報
  * 某節點 `raft_log_lag > 100` ➜ 表示同步落後

---

如你需要我幫你：

* 🧰 加入完整的 log replication、snapshot、follower lag 計算
* 📦 打包 Prometheus + Grafana + raft-node 成 docker-compose 一鍵部署
* 📈 匯出預設 Grafana dashboard JSON 模板

我可以繼續協助，請告訴我你想要的方向。

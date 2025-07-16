好的，我先幫你分兩部分進行：

---

## 🔹 一、Raft 論文《In Search of an Understandable Consensus Algorithm》重點摘要

Raft 是 Diego Ongaro 和 John Ousterhout 在 2014 年提出的一種一致性算法，目標是更容易理解的替代 Paxos，應用在分散式系統中實現一致性（如：狀態機複製 State Machine Replication）。

### ✅ Raft 的三個主要子問題（核心組件）

| 子問題                       | 描述                                                                                   |
| ------------------------- | ------------------------------------------------------------------------------------ |
| 1. Leader Election（領導者選舉） | 節點透過選舉產生一個領導者（Leader），僅有 Leader 可以處理 Client 請求。Follower 超時沒收到心跳後會變成 Candidate 並發起選舉。 |
| 2. Log Replication（日誌複製）  | Leader 接收 client 請求後會將指令記錄在本地 log，並複製到其他 follower，等過半回應後才 commit。                    |
| 3. Safety（安全性）            | 保證即使選出新 Leader，也不會丟失已 commit 的 log，並確保所有節點日誌最終一致。這透過 term、index、投票限制等保證。             |

### ✅ 節點狀態與行為轉換

* 節點角色：

  * Follower（追隨者）
  * Candidate（候選人）
  * Leader（領導者）

* 狀態轉換圖：

```
    Follower
       |
   election timeout
       ↓
   Candidate --- wins election --> Leader
       ↑            |
   receives heartbeat or valid leader
```

### ✅ 記憶項目

* 任期（term）：單調遞增的整數，選舉用
* 投票限制：每任期一個節點只能投一次票
* 心跳（AppendEntries RPC）：Leader 發送維持追隨者關係

---

## 🔹二、使用 Java 17 + Spring Boot 3.2 + Gradle 實作 Raft

接下來是簡化版 Raft：以 3 個節點模擬一個 Raft 系統，使用 RESTful API 作為 RPC 模擬。

### 📁 專案目錄結構（gradle multi-module）

```
raft-demo/
├── settings.gradle
├── build.gradle
└── raft-node/
    ├── build.gradle
    └── src/
        ├── main/
        │   ├── java/com/example/raft/
        │   │   ├── RaftApplication.java
        │   │   ├── RaftNode.java
        │   │   ├── Role.java
        │   │   ├── RaftState.java
        │   │   ├── controller/RaftController.java
        │   │   └── model/AppendEntriesRequest.java
        │   └── resources/
        │       └── application.yml
```

---

### ✅ `RaftState.java`（狀態資料）

```java
public class RaftState {
    public int currentTerm = 0;
    public String votedFor = null;
    public Role role = Role.FOLLOWER;
    public int commitIndex = 0;
    public int lastApplied = 0;

    public synchronized void becomeFollower(int term) {
        this.currentTerm = term;
        this.role = Role.FOLLOWER;
        this.votedFor = null;
    }
}
```

---

### ✅ `Role.java`（節點角色）

```java
public enum Role {
    FOLLOWER,
    CANDIDATE,
    LEADER
}
```

---

### ✅ `RaftNode.java`（核心邏輯）

```java
@Service
public class RaftNode {

    private final RaftState state = new RaftState();
    private final RestTemplate restTemplate = new RestTemplate();
    private final List<String> peers = List.of("http://localhost:8081", "http://localhost:8082");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        startElectionTimer();
    }

    private void startElectionTimer() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (state.role == Role.FOLLOWER) {
                state.role = Role.CANDIDATE;
                startElection();
            }
        }, 5, 5, TimeUnit.SECONDS); // 模擬選舉超時
    }

    private void startElection() {
        state.currentTerm++;
        state.votedFor = "self";
        int votes = 1;

        for (String peer : peers) {
            try {
                ResponseEntity<Boolean> response = restTemplate.postForEntity(peer + "/raft/vote",
                    state.currentTerm, Boolean.class);
                if (Boolean.TRUE.equals(response.getBody())) {
                    votes++;
                }
            } catch (Exception ignored) {}
        }

        if (votes > peers.size() / 2) {
            state.role = Role.LEADER;
            sendHeartbeats();
        } else {
            state.becomeFollower(state.currentTerm);
        }
    }

    private void sendHeartbeats() {
        scheduler.scheduleWithFixedDelay(() -> {
            if (state.role != Role.LEADER) return;
            for (String peer : peers) {
                try {
                    restTemplate.postForEntity(peer + "/raft/append", new AppendEntriesRequest(state.currentTerm), Void.class);
                } catch (Exception ignored) {}
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    public boolean requestVote(int term) {
        if (term > state.currentTerm) {
            state.becomeFollower(term);
            state.votedFor = "other";
            return true;
        }
        return false;
    }

    public void receiveAppendEntries(AppendEntriesRequest req) {
        if (req.term >= state.currentTerm) {
            state.becomeFollower(req.term);
        }
    }
}
```

---

### ✅ `RaftController.java`

```java
@RestController
@RequestMapping("/raft")
public class RaftController {

    @Autowired
    private RaftNode node;

    @PostMapping("/vote")
    public ResponseEntity<Boolean> vote(@RequestBody int term) {
        return ResponseEntity.ok(node.requestVote(term));
    }

    @PostMapping("/append")
    public ResponseEntity<Void> append(@RequestBody AppendEntriesRequest req) {
        node.receiveAppendEntries(req);
        return ResponseEntity.ok().build();
    }
}
```

---

### ✅ `AppendEntriesRequest.java`

```java
public class AppendEntriesRequest {
    public int term;

    public AppendEntriesRequest() {}

    public AppendEntriesRequest(int term) {
        this.term = term;
    }
}
```

---

### ✅ `application.yml`

```yaml
server:
  port: 8080  # 可啟多個節點，修改為 8081, 8082 分別跑不同 port
```

---

## 🔚 後續方向與改進

* ✅ 加入 log replication 與 commitIndex 處理
* ✅ 持久化 log 與快照（snapshot）
* ✅ 使用 WebSocket / gRPC 提升模擬的真實度
* ✅ 增加 cluster discovery（動態節點）

---

如你希望我將這個專案包成可執行的 GitHub repo 範本，或補上完整的 `build.gradle`、多節點啟動 script，我也可以幫你補上。是否需要？

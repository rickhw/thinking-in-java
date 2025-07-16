以下是完整補充，包括：

1. `settings.gradle`
2. `build.gradle`（root 和子模組）
3. 多節點啟動腳本（可用於模擬三個 Raft 節點）
4. 改進的 `application.yml`（支援 port 與節點 ID 配置）

---

## 📁 1. `settings.gradle`

```groovy
rootProject.name = 'raft-demo'
include 'raft-node'
```

---

## 📁 2. Root `build.gradle`

```groovy
plugins {
    id 'java'
}

allprojects {
    group = 'com.example'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }
}
```

---

## 📁 3. 子模組 `raft-node/build.gradle`

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

---

## 📄 4. 改進版 `application.yml`（位於 `raft-node/src/main/resources/`）

```yaml
server:
  port: ${RAFT_PORT:8080}

raft:
  id: ${RAFT_ID:node1}
  peers:
    - http://localhost:8081
    - http://localhost:8082
```

> 註：你可以使用環境變數 `RAFT_PORT` 與 `RAFT_ID` 動態配置不同節點。

---

## 📜 5. 多節點啟動腳本：`run-multiple-nodes.sh`

放在專案根目錄，執行後可模擬三個節點。

```bash
#!/bin/bash

echo "🟢 啟動 3 個 Raft 節點"

./gradlew :raft-node:bootRun --args='--server.port=8080 --RAFT_ID=node1' > /tmp/raft-node1.log 2>&1 &
./gradlew :raft-node:bootRun --args='--server.port=8081 --RAFT_ID=node2' > /tmp/raft-node2.log 2>&1 &
./gradlew :raft-node:bootRun --args='--server.port=8082 --RAFT_ID=node3' > /tmp/raft-node3.log 2>&1 &

echo "✅ 所有節點已啟動，請檢查 /tmp/raft-nodeX.log 以確認運行狀況"
```

---

## 📌 其他補充建議

* 為了更好觀察：建議在 `RaftNode` 中 `System.out.println()` 標示目前角色變化、term、投票等。
* 日誌記錄可加上 SLF4J (`implementation 'org.slf4j:slf4j-api'`)。
* 運行時建議用 `curl` 測試：

  ```bash
  curl -X POST http://localhost:8081/raft/vote -H "Content-Type: application/json" -d 5
  ```

---

如你需要：

* 🌐 加入 log replication（client command）
* 🔁 log 回應與 commit index 機制
* 🧪 測試與 failover 模擬工具
* 📦 Docker Compose 部署版

我可以幫你一步步擴充這個專案。是否要進一步實作 `log replication`？

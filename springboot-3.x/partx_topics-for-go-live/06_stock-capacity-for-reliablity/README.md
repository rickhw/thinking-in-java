
要在 Grafana 中以 Elasticsearch 作為資料來源，並呈現 `capacity.remaining` 和 `capacity.consumed` 的趨勢，可以使用 Lucene Query 來篩選相關數據。

以下是 Lucene Query 和配置步驟：

---

### Lucene Query
```plaintext
app: "asimov" AND module: "capacity" AND capacity.remaining: * AND capacity.consumed: *
```

### 說明
1. **`app: "asimov"`**: 篩選應用名稱為 `asimov` 的日誌。
2. **`module: "capacity"`**: 篩選模組為 `capacity` 的日誌。
3. **`capacity.remaining: *` 和 `capacity.consumed: *`**: 確保 `capacity.remaining` 和 `capacity.consumed` 都有值。

---

### Grafana 配置
1. **新增面板**：
   - 點擊 `Create` -> `Dashboard` -> `Add new panel`。

2. **設定資料來源**：
   - 選擇已配置的 Elasticsearch 資料來源。

3. **設定查詢**：
   - 查詢類型：`Lucene Query`
   - 輸入上方的 Lucene Query。

4. **設定欄位**：
   - 將 `capacity.remaining` 和 `capacity.consumed` 設為 Y 軸的數據來源。
     - 點擊 **Field** -> **Add field**。
     - 選擇 `capacity.remaining` 作為第一條數據。
     - 添加另一條數據來源，選擇 `capacity.consumed`。

5. **設定時間範圍**：
   - 確保 `@timestamp` 已配置為時間欄位。
   - 選擇合適的時間範圍（例如：最近 1 小時或 24 小時）。

6. **設定圖表類型**：
   - 選擇趨勢圖（Time series）。

---

### 測試與調整
- 預覽生成的趨勢圖，確保數據正確顯示。
- 若數據未正確顯示，可嘗試將 Lucene Query 的條件進一步細化，例如：
  ```plaintext
  app: "asimov" AND module: "capacity" AND capacity.remaining: [* TO *] AND capacity.consumed: [* TO *]
  ```

完成後，Grafana 將會顯示 `capacity.remaining` 和 `capacity.consumed` 的趨勢變化。




請幫我用 java 寫一個 Client 程式，這個程式主要要處理大型檔案上傳使用。

首先幫我先寫好大框架的主程式, 這個程式的輸入有以下：

1. 輸入指定檔案位置 (Path)
2. 檔案區塊大小 (ChunkSize), 預設為 500KB
3. 同步上傳數量 (Concurrent)
4. 上傳最長時間 (MaxTime), 預設為 86400 秒, 也就是一天


其中 1), 2), 3), 4) 請幫我透過 config 檔案讀入. 而 path 則透過 arugment 讀取, 如果使用者沒有指定, 則讀取 config, 如果都沒有指定, 則預設是 current path.


---

接下來是是階段一：初始化，請建立另一個 Class 來實做這段，並且適當修改主程式 (FileUploadClient) 。

初始化主要將檔案內容 (無論二進位還是 ASCII)，依照檔案實際的大小、以及區塊大小，整理以下：

- 預計切割的區塊數量
- 檔案屬性 (Content-Type)
- 這個檔案的雜湊值，先使用 md5 做演算法

做好這些準備之後，把相關資訊封裝好成一個 payload。

然後跟 ServerSide 開啟一個 Initial 的通訊，調用 ServerSide 的 API 如下：

```bash
POST /file:initiate

{
    "fileSize": 2345634,
    "content-type": "application/pdf  ",
    "hashcode": "12883c8e55919c89877063733237e906",
    "hash-algo": "md5",
    "chunkSize": "5M",
    "chunkCount": 47,
}
```

ServerSide 回傳如下：

```bash
HTTP/1.1 202 Accepted

{
    "txId": "b3d06bfc-31f3-4ae6-a07f-de7b7ecf3ce6"
}
```

這個階段的每個處理，請分別用 method 拆分。


---

階段二：

ServerSide 已經收到 Initial 請求，然後告訴 Client Side 說你可以開始上傳了，這時候 Client 就會依照開始做以下的任務：

1. 依照 ChunkSize 切割檔案，打包
2. 依照 Concurrent 數量，開啟 Thread 上傳

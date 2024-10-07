在 Linux 系統中，除了 htop 之外，還有許多 CLI 工具可以幫助你監控 Java 應用程式的狀況，包括進程、子進程、打開的檔案、網路 socket 等。以下是一些常用工具和指令：

## 1. ps (Process Status)

用來查看系統中的進程，可以顯示 Java 應用的 PID、CPU、記憶體、執行狀態等。

查看所有 Java 進程：

```bash
ps aux | grep java
```

查看特定 Java 進程的詳細資訊：

```bash
ps -p <PID> -o pid,user,%cpu,%mem,vsz,rss,tty,stat,start,time,cmd
```
其中，<PID> 是 Java 應用程式的進程 ID。

## 2. top

類似於 htop，top 也能顯示系統進程的即時資訊，但顯示形式較為簡潔。

啟動後可以按下 M 鍵根據記憶體使用量排序，按 P 鍵根據 CPU 使用量排序。
使用 top 過濾 Java 進程：

```bash
top -p <PID>
```

## 3. lsof (List Open Files)

可以列出指定進程打開的檔案（包括 socket、網路連接、設備等）。

查看某個 Java 進程打開的檔案：

```bash
lsof -p <PID>
```

列出所有 Java 進程相關的打開檔案：

```bash
lsof | grep java
```

## 4. netstat / ss (Network Statistics)

用來檢查網路連接和 socket 狀況。ss 是 netstat 的替代工具，執行速度更快。

查看所有開啟的網路連接（包含 socket）：

```bash
netstat -plant | grep java
```

或使用 ss：

```bash
ss -plant | grep java
```

其中，-p 用來顯示進程，-a 顯示所有 socket，-l 顯示監聽的 socket，-n 以數字顯示 IP。

## 5. pstree

顯示進程樹狀結構，便於查看 Java 應用的主進程及其子進程之間的關係。

查看特定進程的樹狀結構：

```bash
pstree -p <PID>
```

查看所有 Java 進程的樹狀結構：

```bash
pstree -p | grep java
```

## 6. pmap (Process Memory Map)

用來查看進程的記憶體映射，特別是每個記憶體區域的使用情況。

查看某個 Java 進程的記憶體映射：

```bash
pmap -x <PID>
```

總結記憶體使用情況：

```bash
pmap <PID> | tail -n 1
```

## 7. strace (System Call Trace)

用來跟蹤進程的系統調用。這對於監控 Java 應用如何與系統互動非常有用。

跟蹤某個 Java 進程的系統調用：

```bash
strace -p <PID>
```

如果你只想跟蹤網路調用：

```bash
strace -p <PID> -e trace=network
```

## 8. jcmd (Java Diagnostic Commands)

這是 JDK 提供的工具，能夠查詢 Java 進程的詳細狀況，包括堆內存、執行緒和其他 JVM 診斷資訊。

列出所有 Java 進程：

```bash
jcmd
```

查看堆內存狀況：

```bash
jcmd <PID> GC.heap_info
```

列出所有 JVM 執行緒：

```bash
jcmd <PID> Thread.print
```

## 9. jstack (Java Stack Trace)

可以獲取指定進程的執行緒堆棧追蹤，通常用於排查死鎖或效能瓶頸。

查看 Java 應用的執行緒堆棧：

```bash
jstack <PID>
```

## 10. jmap (Memory Map)

用來獲取 JVM 內存使用情況的工具，可以生成 heap dump 或檢查堆內存狀況。

查看 Java 堆內存摘要：

```bash
jmap -heap <PID>
```

生成 heap dump（可用於後續分析）：

```bash
jmap -dump:live,format=b,file=heap_dump.hprof <PID>
```

##總結

這些工具都可以從不同的角度監控 Java 應用程式的狀況，從系統層級的進程、記憶體、打開的檔案與網路連接，到 JVM 特有的執行緒和堆內存分析。選擇適合你的需求的工具，並根據應用程式的具體情況進行監控和診斷。
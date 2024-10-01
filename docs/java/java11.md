Java 11 是 Oracle 發布的長期支持版本（LTS），於 2018 年 9 月發布。它繼承了 Java 9 和 Java 10 中的許多變更，並引入了一些重要的新功能和改進。以下是 Java 11 的主要新功能及其與前一版本 Java 10 的差異：

## 1. var 用於局部變量的類型推斷

功能: Java 11 引入了 var 關鍵字來進行局部變量的類型推斷。這在 Java 10 中已經出現，Java 11 進一步穩定了該功能。

範例:

```java
var message = "Hello, Java 11!";
```

差異: Java 10 是第一個支持 var 的版本，Java 11 繼續保留該功能並增強了其在局部作用域中的應用。

### 2. 運行 Java 文件的簡化

功能: Java 11 支持直接運行 .java 文件，無需先編譯成 .class 文件，這對於小型測試代碼和腳本化應用來說非常方便。

範例:

```bash
java HelloWorld.java
```

差異: 在 Java 10 及之前，Java 代碼必須先用 javac 編譯成 .class 文件，然後才能運行。Java 11 簡化了這一流程，對於簡單程序和快速測試特別有用。

##3. HTTP Client API 的正式化

功能: Java 11 將在 Java 9 和 Java 10 中預覽的 HTTP Client API (基於 java.net.http) 正式納入標準庫。這個 API 支持現代化的 HTTP/2 協議，並簡化了 HTTP 請求和響應處理。

範例:

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://example.com"))
    .build();
HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

差異: Java 10 中的 HTTP Client API 仍處於預覽階段，Java 11 將其正式化，取代了老舊的 HttpURLConnection，提供了更現代化的 HTTP 請求處理方式。


## 4. 垃圾回收器改進

功能: Java 11 引入了 ZGC (Z Garbage Collector)，這是一種可擴展的、低延遲的垃圾回收器，旨在對應對大規模應用程序中的垃圾回收延遲問題。
差異: Java 10 中引入了 G1 GC 的增強版，但 Java 11 則進一步通過 ZGC 提供了更強大的垃圾回收選項，特別適合對響應時間敏感的大型系統。


## 5. String 類的新方法

功能: Java 11 增加了一些實用的 String 方法，簡化了字符串處理。

- isBlank()：檢查字符串是否為空白（即使有空格也算空白）。
- lines()：將字符串分割為多行並返回 Stream。
- strip()：移除字符串首尾的空白（支持 Unicode）。
- repeat(int)：重複字符串指定次數。

範例:

```java
String str = "   ";
System.out.println(str.isBlank()); // true
System.out.println("abc\n123".lines().count()); // 2
System.out.println("  hello ".strip()); // "hello"
System.out.println("ha".repeat(3)); // "hahaha"
```

差異: 這些方法在 Java 10 中還沒有，Java 11 的新增方法大大增強了對字符串操作的便利性，減少了樣板代碼。


## 6. 棄用和移除 Java EE 和 CORBA 模塊

- 功能: Java 11 移除了不再推薦使用的 Java EE（如 JAXB、JAX-WS）和 CORBA 模塊。這些模塊之前在 Java 9 中已經被標記為過時。
- 差異: Java 9 和 Java 10 只是將這些模塊標記為不推薦，Java 11 正式將它們移除，這使得 Java 標準庫更加輕量化和現代化。

## 7. Lambda 表達式和 Functional Interface 的局部變量語法改進

功能: Java 11 允許在 Lambda 表達式的參數中使用 var 關鍵字，以提高一致性和可讀性。

範例:

```java
var sum = (var a, var b) -> a + b;
```

差異: 在 Java 10 之前，Lambda 表達式的參數不能使用 var。Java 11 加強了 Lambda 表達式的語法靈活性，使代碼風格更加一致。


## 8. Flight Recorder

- 功能: Java 11 中，Oracle 將 JDK Flight Recorder (JFR) 免費開放。這是一種低開銷的性能監控和故障排查工具，允許開發者捕獲 Java 應用在運行過程中的性能數據。
- 差異: 在 Java 10 及之前，Flight Recorder 是商業版 JDK 的一部分，而在 Java 11，它變得免費並整合在開源 JDK 中，讓更多開發者受益。


## 9. Epsilon 垃圾回收器

- 功能: Java 11 引入了 Epsilon GC，這是一個「無操作」的垃圾回收器，用於測試場景中，允許開發者觀察應用在沒有垃圾回收的情況下如何運行。
- 差異: 這是 Java 11 中的新功能，適合在性能測試或記憶體壓力測試中使用。Java 10 中並沒有這種測試專用的 GC。

## 總結 Java 11 的新功能與 Java 10 的差異：

- 更多工具: Java 11 引入了直接運行 Java 文件的功能，增強了對開發者的支持，使得編寫和運行代碼更加高效。
- API 穩定化: Java 10 中的預覽 API（如 HTTP Client API）在 Java 11 中正式化，為網絡通信提供了現代化的處理方式。
- 垃圾回收器選擇更多: Java 11 引入了 ZGC 和 Epsilon GC，提供了針對不同需求的垃圾回收策略，提升了應用程序的可擴展性和靈活性。
- 棄用與移除: Java 11 移除了不再推薦的舊技術，如 Java EE 和 CORBA，清理了標準庫，推動了更加現代化的應用程序設計。

Java 11 以其穩定性和長期支持（LTS）的優勢，成為了許多企業和開發者的首選版本，相比 Java 10 提供了更加成熟和現代化的開發環境。
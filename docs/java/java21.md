Java 21 是一個具備長期支持（LTS）的版本，於 2023 年 9 月發布，帶來了大量的新功能和增強。以下是 Java 21 的主要新功能以及與前一個 LTS 版本（Java 17）的差異。

## 1. 記錄模式（Record Patterns）

- 功能: 記錄模式是模式匹配的一部分，允許你在 switch 語句或 if 語句中直接解構 Record，讓代碼更簡潔直觀。

範例:

```java
record Point(int x, int y) {}

static void printPoint(Object obj) {
    if (obj instanceof Point(int x, int y)) {
        System.out.println("x = " + x + ", y = " + y);
    }
}
```
差異: Java 17 引入了 Record 作為一種更簡潔的數據類型，但在模式匹配中未有進一步擴展。Java 21 則整合了記錄模式和模式匹配的使用。


## 2. 模式匹配的增強

- 功能: Java 21 擴展了模式匹配（Pattern Matching），現在可以在 switch 表達式中使用更復雜的模式，並支持對多層結構進行解構。

範例:

```java
static String typeCheck(Object obj) {
    return switch (obj) {
        case String s -> "It's a String";
        case Integer i -> "It's an Integer";
        case null -> "It's null";
        default -> "Unknown";
    };
}
```

差異: Java 17 引入了針對 instanceof 的模式匹配，但模式匹配在 switch 中的應用還處於測試階段。Java 21 大幅度擴展了模式匹配的能力。


## 3. 向量 API（Vector API）

功能: 向量 API 讓開發者可以利用 CPU 向量化的能力進行高效的數據處理，特別適合於科學計算或需要高效處理數據的應用場景。
範例:

```java
VectorSpecies<Float> species = FloatVector.SPECIES_256;
FloatVector vector1 = FloatVector.fromArray(species, array1, 0);
FloatVector vector2 = FloatVector.fromArray(species, array2, 0);
FloatVector result = vector1.add(vector2);
result.intoArray(resultArray, 0);
```

差異: Java 17 開始引入向量 API 的測試功能，但仍然處於孵化階段。Java 21 進一步改進了該 API，使其更穩定和實用。


## 4. 異常處理的結構化並發

功能: Java 21 引入了結構化並發，允許開發者更簡單、更直觀地管理多線程和異常處理。這種設計讓多線程任務可以像同步代碼一樣自然地表達。

範例:

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<Integer> result1 = scope.fork(() -> task1());
    Future<Integer> result2 = scope.fork(() -> task2());
    scope.join();
    return result1.resultNow() + result2.resultNow();
}
```

差異: Java 17 並沒有提供如此直觀的結構化並發工具。Java 21 的這一功能大大簡化了多線程編程，特別是在處理異常時。


## 5. 字面量字符串模板（String Templates）

功能: Java 21 引入了字面量字符串模板，這是一種嵌入表達式的字符串形式，類似於其他語言中的模板字符串，這使得字符串格式化變得更加方便和強大。

範例:

```java
String name = "Java";
int age = 29;
String message = STR."Hello, my name is \{name} and I am \{age} years old.";
```

差異: 在 Java 17 及之前，字符串的處理主要依賴於 String.format() 或使用外部庫。Java 21 通過模板字符串提供了一種更簡潔的格式化方式。


## 6. 虛擬線程（Virtual Threads）

功能: Java 21 將虛擬線程（Project Loom 的一部分）引入正式版本。虛擬線程是輕量級的線程，能夠以更低的開銷執行大規模的並發任務。

範例:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // 虛擬線程代碼
    });
}
```

差異: Java 17 中，線程的開銷較大，特別是在處理高併發的情況下。Java 21 的虛擬線程顯著降低了線程開銷，適合高併發的應用。


## 7. 序列化過濾器（Serialization Filters）

- 功能: Java 21 引入了序列化過濾器的增強功能，以提高 Java 序列化的安全性，讓開發者可以更好地控制哪些類型可以被反序列化，防止反序列化攻擊。
- 差異: Java 17 中，序列化過濾器的配置選項有限，Java 21 進一步增強了過濾器的靈活性。


## 8. 更好的垃圾回收（Garbage Collection）

- 功能: Java 21 進一步優化了 G1 和 ZGC 垃圾回收器，特別是在大型應用和低延遲應用中的表現。
- 差異: 雖然 Java 17 已經有相當優化的 G1 和 ZGC，但 Java 21 提供了更多的性能改進和穩定性提升。


## 9. 泛型的無界型（Unrestricted Generics）

- 功能: Java 21 開始引入對泛型無界型的支持，讓泛型系統更加靈活，這是未來版本中計劃逐步推廣的特性。
- 差異: Java 17 的泛型系統相對保守，無法實現無界型的靈活性。Java 21 開始在這方面進行嘗試。


## 總結 Java 21 與 Java 17 的差異

- 更現代的語法與模板: Java 21 引入了模板字符串和更豐富的模式匹配，這些特性在 Java 17 中並不存在。
- 併發處理大幅改進: Java 21 提供了虛擬線程和結構化並發，大大簡化了高併發應用的編寫與管理，而這在 Java 17 中仍然依賴傳統的線程模型。
- 向量 API 和數據處理的改進: Java 21 的向量 API 更加成熟，適合進行大規模數據處理。
- 垃圾回收器進一步優化: Java 21 對 G1 和 ZGC 進行了更進一步的優化，特別是在低延遲場景中提供更好的表現。

Java 21 在語法簡化、高併發處理、性能提升等方面都帶來了顯著的增強，特別是對於現代應用場景下的性能和可維護性提供了更強大的支持。
Java 17 是一個長期支持 (LTS) 版本，包含了許多新功能和增強功能，相較於前一個 LTS 版本 Java 11，提供了更強大的功能和語法改進。以下是 Java 17 的主要新功能及與先前版本的差異：

## 1. Sealed Classes (密封類別)

功能: 允許開發者限制哪些類別或介面可以繼承或實作密封類別或介面。
語法: 使用 sealed 關鍵字來聲明類別，並指定哪些子類別可以擴展它。

範例:
```java
public sealed class Vehicle permits Car, Truck {}
public final class Car extends Vehicle {}
public final class Truck extends Vehicle {}
```

差異: 在 Java 11 中，無法限制繼承，開發者無法對子類別進行控制。而 Java 17 的密封類別使得繼承結構更嚴謹，適合用於安全性和清晰的架構設計。

## 2. Pattern Matching for instanceof (模式匹配)

功能: 在 instanceof 操作符中進行類型檢查後自動轉型，減少冗長的轉型代碼。

範例:

```java
if (obj instanceof String s) {
    System.out.println(s.toLowerCase());
}
```

差異: 在 Java 11 中，使用 instanceof 檢查後仍需明確進行類型轉型。Java 17 簡化了代碼，減少轉型的樣板代碼，提高可讀性。


## 3. Records (紀錄類型) - 進一步擴展

功能: Record 是一種輕量級類型，適合只存儲數據的類別，Java 17 提供了進一步的擴展，如允許 Record 進行局部類別宣告等。

範例:

```java
public record Point(int x, int y) {}
```

差異: Java 11 不支援 Record，這是 Java 14 引入的語法，而在 Java 17 中，它已完全穩定並擴展，能在簡單資料封裝中減少冗餘代碼。


## 4. Text Blocks (文字區塊)

功能: 支援多行字串文字，方便書寫格式化文本，如 JSON、SQL 等。

範例:

```java
String json = """
    {
        "name": "John",
        "age": 30
    }
    """;
```

差異: 在 Java 11 中，可以使用 String::repeat 等方法，但沒有支援多行文字區塊。在 Java 17 中，文字區塊功能已完全穩定，方便處理多行字串資料。


## 5. Switch 表達式 (進一步擴展)

功能: switch 可以作為表達式來使用，且支援模式匹配。

範例:

```java
int day = switch (dayOfWeek) {
    case MONDAY, FRIDAY, SUNDAY -> 6;
    case TUESDAY -> 7;
    default -> throw new IllegalStateException("Invalid day: " + dayOfWeek);
};
```

差異: Java 11 的 switch 僅能作為語句，而無法作為表達式使用。Java 17 的擴展提供了更簡潔、功能更強大的語法。

## 6. Foreign Function & Memory API (外部函數與記憶體 API) [預覽]

- 功能: 引入新的 API 來訪問外部函數和記憶體，取代 Java Native Interface (JNI) 的複雜性。
- 差異: Java 11 需要使用 JNI 來訪問本地代碼，但這通常比較複雜且效率低下。Java 17 引入了更現代化且高效的方式來處理此問題，並且簡化了跨語言互操作。

---
## 7. Vector API (向量 API) [預覽]

- 功能: 提供一種向量操作的 API，能夠充分利用硬體的 SIMD (單指令多數據) 功能來提高運算效能。
- 差異: Java 11 並不原生支援向量操作，開發者需使用外部函式庫來實現。而在 Java 17 中，向量 API 大幅簡化了高效數據處理的實現。


## 8. 強化 NullPointerException 診斷

- 功能: Java 17 提供了更詳細的 NullPointerException 堆棧資訊，能夠更精準地定位錯誤。

範例:

```java
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.length()" because "str" is null
```

差異: Java 11 中的 NullPointerException 堆棧資訊較少，需手動調試才能查明原因。Java 17 提供了更直觀的錯誤訊息，減少了除錯的時間。

## 9. Deprecation & Removal (棄用與移除功能)

- Applets: 已完全移除，不再支援。
- Security Manager: 標記為過時，預計將在未來版本移除。

## 總結差異

- 語法簡化: 新功能如模式匹配、密封類別、Record 等，減少了樣板代碼，提高了代碼可讀性和可維護性。
- 性能增強: 新的 API（如外部函數、向量 API）為高效處理外部資源和大數據計算提供了現代化的解決方案。
- 錯誤診斷改善: 更強大的 NullPointerException 報告使得錯誤調試更方便。

Java 17 不僅提供了語法上的改進，還強化了效能與開發體驗，使其成為現代化應用程式的理想選擇。

## Chat GPT

使用 Jackson 來處理 Java 中的 JSON，需要先了解一些前提條件與基本規則，以下是整理的要點與範例程式：

### 前提條件

1. **依賴設定**：在你的專案中添加 Jackson 的依賴。如果使用 Maven 或 Gradle，需要將 Jackson 庫引入。

    - **Gradle**：
      ```groovy
      implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
      ```
    - **Maven**：
      ```xml
      <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
      </dependency>
      ```

2. **核心類別**：
   - `ObjectMapper` 是核心類別，用於將 Java 對象與 JSON 之間進行序列化與反序列化。
   - `JsonNode` 用來處理未知或動態結構的 JSON。

3. **POJO (Plain Old Java Object)**：
   - 進行 JSON 與 Java 對象轉換時，POJO 的屬性名稱應與 JSON 中的字段名稱一致。如果字段名稱不一致，可以使用 `@JsonProperty` 注解來指定對應的 JSON 字段名稱。

### 基本規則

1. **JSON 與 POJO 的映射**：
   - Jackson 預設使用反射來自動映射 JSON 與 POJO 的字段，但必須保證 POJO 具有無參構造函數及 getter 和 setter 方法。

2. **序列化**：將 Java 對象轉換為 JSON。
   - `ObjectMapper.writeValue()` 或 `ObjectMapper.writeValueAsString()` 用於將對象轉換為 JSON 字符串。

3. **反序列化**：將 JSON 轉換為 Java 對象。
   - `ObjectMapper.readValue()` 用於將 JSON 字符串轉換回 POJO。

4. **處理 JSON 與動態類型**：
   - 使用 `JsonNode` 處理結構不固定或動態的 JSON。`JsonNode` 提供多種方法來讀取與操作 JSON 樹狀結構。

### 範例程式

#### 1. 序列化範例：Java 對象轉換為 JSON

```java
import com.fasterxml.jackson.databind.ObjectMapper;

class User {
    private String name;
    private int age;

    // Constructors, getters, and setters
    public User() {}
    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

public class JacksonExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 創建一個 User 對象
        User user = new User("Alice", 30);

        // 將對象序列化為 JSON 字符串
        String jsonString = mapper.writeValueAsString(user);
        System.out.println("Serialized JSON: " + jsonString);
    }
}
```

輸出：
```json
Serialized JSON: {"name":"Alice","age":30}
```

#### 2. 反序列化範例：JSON 轉換為 Java 對象

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // JSON 字符串
        String jsonString = "{\"name\":\"Alice\",\"age\":30}";

        // 反序列化為 User 對象
        User user = mapper.readValue(jsonString, User.class);

        System.out.println("User Name: " + user.getName());
        System.out.println("User Age: " + user.getAge());
    }
}
```

輸出：
```
User Name: Alice
User Age: 30
```

#### 3. 動態結構處理範例：使用 `JsonNode`

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // JSON 字符串
        String jsonString = "{\"name\":\"Alice\",\"age\":30, \"skills\":[\"Java\",\"Spring\"]}";

        // 解析為 JsonNode
        JsonNode node = mapper.readTree(jsonString);

        // 獲取數據
        String name = node.get("name").asText();
        int age = node.get("age").asInt();
        JsonNode skills = node.get("skills");

        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Skills: " + skills);
    }
}
```

輸出：
```
Name: Alice
Age: 30
Skills: ["Java","Spring"]
```

### 小結
1. **依賴管理**：引入 Jackson 的依賴，使用 `ObjectMapper` 處理 JSON。
2. **POJO 映射**：POJO 必須符合無參構造及 getter/setter 規則，且字段名應與 JSON 字段匹配。
3. **動態處理**：對於未知結構的 JSON，可以使用 `JsonNode` 進行靈活處理。

透過這些基礎規則與範例，你可以有效地使用 Jackson 來處理各種 JSON 操作。



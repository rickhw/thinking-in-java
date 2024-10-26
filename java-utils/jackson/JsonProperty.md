
## JsonProperty

`@JsonProperty` 注解可用於定義 Java 對象的字段與 JSON 字段之間的對應關係，尤其當 JSON 字段名稱與 Java 字段名稱不一致時特別有用。以下是使用 `@JsonProperty` 的範例，展示如何處理不同名稱的字段。

### 範例說明

假設我們有一個 JSON 文件，字段名使用了下劃線（snake_case），而 Java 中的對象則使用駝峰命名法（camelCase）。可以通過 `@JsonProperty` 注解來指定具體的 JSON 字段名稱與 Java 字段之間的對應關係。

### 範例程式

#### 1. 使用 `@JsonProperty` 來匹配不同的字段名稱

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

class Person {
    // 使用 @JsonProperty 註解指定 JSON 中的字段名
    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private int age;

    // Constructors, getters, and setters
    public Person() {}

    public Person(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}

public class JsonPropertyExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // JSON 字符串，字段名使用下劃線格式
        String jsonString = "{\"first_name\":\"John\", \"last_name\":\"Doe\", \"age\":25}";

        // 反序列化為 Person 對象
        Person person = mapper.readValue(jsonString, Person.class);

        // 顯示反序列化結果
        System.out.println("First Name: " + person.getFirstName());
        System.out.println("Last Name: " + person.getLastName());
        System.out.println("Age: " + person.getAge());

        // 將 Person 對象序列化為 JSON
        String serializedJson = mapper.writeValueAsString(person);
        System.out.println("Serialized JSON: " + serializedJson);
    }
}
```

### 範例解析

1. **反序列化**：
   - JSON 文件中的字段名是 `"first_name"` 和 `"last_name"`，但在 Java 中使用的是 `firstName` 和 `lastName`。
   - 使用 `@JsonProperty("first_name")` 來告訴 Jackson，當它看到 `"first_name"` 時，應將其映射到 `firstName` 字段。

2. **序列化**：
   - 當 Java 對象需要被轉換為 JSON 時，Jackson 將會根據 `@JsonProperty` 指定的名稱來生成對應的 JSON 字段。

### 輸出結果

```
First Name: John
Last Name: Doe
Age: 25
Serialized JSON: {"first_name":"John","last_name":"Doe","age":25}
```

### 小結
`@JsonProperty` 可以用來解決 JSON 字段名稱與 Java 字段名稱不一致的情況，無論是在反序列化還是序列化的過程中，這樣可以保持代碼風格與 JSON 結構的靈活性。例如，當外部 API 使用不同命名規則時，可以輕鬆進行映射而不影響代碼的可讀性。
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


----



Lombok 是一個 Java 的庫，用於簡化 Java 類中常見樣板代碼（如 getter、setter、equals、hashCode、toString 等）。通過使用簡單的注解，Lombok 可以自動生成這些方法，從而提高代碼可讀性和可維護性。以下是有關 Lombok 的使用前提、常用範例，以及與其他庫配合使用時的注意事項。

### 使用前提

1. **Lombok 依賴設定**：
   - **Gradle**：
     ```groovy
     implementation 'org.projectlombok:lombok:1.18.28'
     annotationProcessor 'org.projectlombok:lombok:1.18.28'
     ```
   - **Maven**：
     ```xml
     <dependency>
         <groupId>org.projectlombok</groupId>
         <artifactId>lombok</artifactId>
         <version>1.18.28</version>
         <scope>provided</scope>
     </dependency>
     <annotationProcessorPaths>
         <path>
             <groupId>org.projectlombok</groupId>
             <artifactId>lombok</artifactId>
             <version>1.18.28</version>
         </path>
     </annotationProcessorPaths>
     ```

2. **IDE 支持**：
   - **Eclipse/IntelliJ IDEA**：Lombok 需要在 IDE 中啟用插件來正確地處理注解和生成代碼。
     - Eclipse：可以通過官方的 Lombok jar 文件啟用插件。
     - IntelliJ IDEA：直接通過插件市場搜索並安裝 "Lombok Plugin"。

3. **編譯器設置**：
   - 確保編譯器配置正確，以處理 Lombok 的注解處理器，特別是在使用 Maven/Gradle 進行構建時，`annotationProcessor` 路徑必須配置妥當。

### 常用範例

1. **@Getter 和 @Setter**：
   Lombok 自動生成 getter 和 setter 方法，避免手動編寫。
   
   ```java
   import lombok.Getter;
   import lombok.Setter;

   public class User {
       @Getter @Setter
       private String name;
       @Getter @Setter
       private int age;
   }
   ```

   生成的代碼：
   ```java
   public String getName() { return name; }
   public void setName(String name) { this.name = name; }
   public int getAge() { return age; }
   public void setAge(int age) { this.age = age; }
   ```

2. **@ToString**：
   自動生成 `toString()` 方法，可以選擇排除特定字段或僅包含部分字段。
   
   ```java
   import lombok.ToString;

   @ToString(exclude = "password")
   public class User {
       private String name;
       private int age;
       private String password;
   }
   ```

   生成的 `toString()` 方法會排除 `password` 字段：
   ```java
   public String toString() {
       return "User(name=" + this.name + ", age=" + this.age + ")";
   }
   ```

3. **@NoArgsConstructor、@AllArgsConstructor、@RequiredArgsConstructor**：
   自動生成構造方法，根據需求生成無參構造器、全部字段參數的構造器或僅包含必需字段的構造器。

   ```java
   import lombok.NoArgsConstructor;
   import lombok.AllArgsConstructor;
   import lombok.RequiredArgsConstructor;

   @NoArgsConstructor
   @AllArgsConstructor
   public class User {
       private String name;
       private int age;
   }
   ```

4. **@Builder**：
   Lombok 的 `@Builder` 注解可自動生成建造者模式，方便對象的構建。

   ```java
   import lombok.Builder;

   @Builder
   public class User {
       private String name;
       private int age;
   }

   public static void main(String[] args) {
       User user = User.builder()
           .name("Alice")
           .age(30)
           .build();
   }
   ```

5. **@Data**：
   集合了 `@Getter`、`@Setter`、`@ToString`、`@EqualsAndHashCode`、`@RequiredArgsConstructor` 等注解的綜合用法，非常常用。

   ```java
   import lombok.Data;

   @Data
   public class User {
       private String name;
       private int age;
   }
   ```

6. **@EqualsAndHashCode**：
   自動生成 `equals()` 和 `hashCode()` 方法，並可根據需要自定義比較邏輯。

   ```java
   import lombok.EqualsAndHashCode;

   @EqualsAndHashCode
   public class User {
       private String name;
       private int age;
   }
   ```

### 與其他庫使用時的注意事項

1. **與 Jackson 結合使用**：
   - 如果你的項目同時使用 Jackson 來進行 JSON 解析，Lombok 的 `@Getter` 和 `@Setter` 可以與 Jackson 的注解如 `@JsonProperty` 一起使用。但要注意某些情況下，Jackson 可能會因為沒有可訪問的構造器而出錯，這時需要確保加上 `@NoArgsConstructor` 或 `@AllArgsConstructor`。
   
   例如：
   ```java
   import com.fasterxml.jackson.annotation.JsonProperty;
   import lombok.AllArgsConstructor;
   import lombok.NoArgsConstructor;
   import lombok.Data;

   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class User {
       @JsonProperty("user_name")
       private String name;
       private int age;
   }
   ```

2. **與 JPA 結合使用**：
   - 當使用 Lombok 與 JPA (如 Hibernate) 一起時，需要注意 `@EqualsAndHashCode` 和 `@Data` 的使用。因為 JPA 要求代理對象的 `equals()` 和 `hashCode()` 必須根據唯一標識符（如 `@Id` 字段）進行計算，否則可能導致意外行為。
   - 解決方案是手動指定 `@EqualsAndHashCode` 來只比較 `id`。

   ```java
   import lombok.EqualsAndHashCode;
   import javax.persistence.Entity;
   import javax.persistence.Id;

   @Entity
   @EqualsAndHashCode(onlyExplicitlyIncluded = true)
   public class User {
       @Id
       @EqualsAndHashCode.Include
       private Long id;
       
       private String name;
       private int age;
   }
   ```

3. **與 Spring 結合使用**：
   - Lombok 與 Spring Framework 配合使用時通常沒有衝突，但如果使用 `@ConfigurationProperties` 進行屬性綁定時，記得使用 `@Data` 或 `@Getter/@Setter`，並確保擁有無參構造器。

### 小結
Lombok 是一個極大地減少樣板代碼的工具，能夠使代碼更加簡潔易讀，但在與其他庫（如 Jackson、JPA、Spring）結合使用時，需要留意特定場景下可能的衝突或不兼容性。通常，添加正確的構造器或手動定義 `equals()` 和 `hashCode()` 的行為即可解決這些問題。
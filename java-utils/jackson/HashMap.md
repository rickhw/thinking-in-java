
您是對的，使用 Lombok 的 `@Getter` 和 `@Setter` 時，如果需要同時處理 `HashMap` 的基礎結構，必須手動重寫 `put` 操作，以確保字段更新時反映在 Map 結構中。這種情況下，Lombok 無法自動針對 HashMap 的值變更進行額外的處理。

不過，我們可以使用 Lombok 的 `@Delegate` 來達到類似的效果，將 HashMap 的方法委派給內部 `Map` 字段，以減少手動編寫的樣板代碼。這樣可以避免直接繼承 `HashMap`，並使代碼簡化。以下是優化過的寫法。

### 使用 @Delegate 簡化 Map 寫法的範例

我們將 `UserPropertiesMap` 類中的 `HashMap` 行為委派給內部的 `map` 字段，並利用 `@Getter` 和 `@Setter` 來管理自定義字段：

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.util.HashMap;
import java.util.Map;

public class UserPropertiesMap {

    // 使用 @Delegate 委派 Map 操作
    @Delegate
    private final Map<String, Object> map = new HashMap<>();

    @JsonProperty("user_name")
    @Getter @Setter
    private String userName;

    @JsonProperty("user_age")
    @Getter @Setter
    private int age;

    // 重寫 setter，將值同步到 map
    public void setUserName(String userName) {
        this.userName = userName;
        map.put("user_name", userName);
    }

    public void setAge(int age) {
        this.age = age;
        map.put("user_age", age);
    }
}
```

### 主程式範例

```java
public class JsonPropertyMapExample {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // JSON 字符串，包含 user_name 和 user_age 字段
        String jsonString = "{\"user_name\":\"Alice\", \"user_age\":28}";

        // 反序列化 JSON 到 UserPropertiesMap 對象
        UserPropertiesMap userProperties = mapper.readValue(jsonString, UserPropertiesMap.class);

        // 顯示反序列化結果
        System.out.println("User Name: " + userProperties.getUserName());
        System.out.println("Age: " + userProperties.getAge());

        // 序列化為 JSON
        String serializedJson = mapper.writeValueAsString(userProperties);
        System.out.println("Serialized JSON: " + serializedJson);
    }
}
```

### 使用 @Delegate 的效果

1. **Map 委派**：`@Delegate` 將 `HashMap` 的方法（如 `put`, `get`, `containsKey` 等）委派給內部的 `map` 字段，這樣您就不需要繼承 `HashMap`，而仍然可以直接操作 `map` 中的鍵值對。

2. **同步更新**：重寫 `setUserName` 和 `setAge` 方法，以確保當字段更新時，`map` 中的值也會更新。

### 優勢與注意事項

這種寫法的優點是不用直接繼承 `HashMap` 並且代碼更加清晰，但 `@Delegate` 的作用範圍僅限於委派給特定字段的方法，不適合所有情況。
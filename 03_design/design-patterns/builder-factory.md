
以下是 **Factory Pattern** 和 **Builder Pattern** 的特性與差異，並輔以 Java 範例進行說明：

---

## **1. Factory Pattern**

### **特性**
- **目的**：用來簡化物件創建的過程，隱藏具體實現細節。
- **核心思想**：提供一個工廠類別（Factory Class），根據傳入的參數或條件，返回適當的物件實例。
- **適用場景**：
  - 當類別的實例化邏輯較複雜時。
  - 客戶端不需要知道具體類別名稱。
  - 需要根據條件動態決定創建哪種類型的物件。

### **優缺點**
- **優點**：降低耦合，讓系統更具彈性和可擴展性。
- **缺點**：新增產品類別時需要修改工廠邏輯。

### **Java 範例**

#### 定義產品介面：
```java
public interface Shape {
    void draw();
}
```

#### 具體實現類別：
```java
public class Circle implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing a Circle");
    }
}

public class Square implements Shape {
    @Override
    public void draw() {
        System.out.println("Drawing a Square");
    }
}
```

#### 工廠類別：
```java
public class ShapeFactory {
    public static Shape getShape(String shapeType) {
        if (shapeType == null) {
            return null;
        }
        switch (shapeType.toLowerCase()) {
            case "circle":
                return new Circle();
            case "square":
                return new Square();
            default:
                throw new IllegalArgumentException("Unknown shape type: " + shapeType);
        }
    }
}
```

#### 使用工廠：
```java
public class Main {
    public static void main(String[] args) {
        Shape circle = ShapeFactory.getShape("circle");
        circle.draw();

        Shape square = ShapeFactory.getShape("square");
        square.draw();
    }
}
```

---

## **2. Builder Pattern**

### **特性**
- **目的**：將物件的構建過程與表示分離，適合用於構建複雜物件。
- **核心思想**：使用一個 Builder 類別，逐步構造物件，最後生成完整的實例。
- **適用場景**：
  - 當物件有很多屬性且屬性之間有依賴關係時。
  - 需要支持不同的物件組合（例如可選參數）。
  - 想要避免多參數建構子帶來的困難。

### **優缺點**
- **優點**：提高代碼可讀性，易於維護，減少建構子過多的問題。
- **缺點**：可能增加額外的類別或代碼。

### **Java 範例**

#### 定義要構建的物件：
```java
public class Computer {
    private String CPU;
    private int RAM;
    private int storage;
    private boolean hasGraphicsCard;

    private Computer(Builder builder) {
        this.CPU = builder.CPU;
        this.RAM = builder.RAM;
        this.storage = builder.storage;
        this.hasGraphicsCard = builder.hasGraphicsCard;
    }

    @Override
    public String toString() {
        return "Computer{" +
               "CPU='" + CPU + '\'' +
               ", RAM=" + RAM +
               ", storage=" + storage +
               ", hasGraphicsCard=" + hasGraphicsCard +
               '}';
    }

    public static class Builder {
        private String CPU;
        private int RAM;
        private int storage;
        private boolean hasGraphicsCard;

        public Builder setCPU(String CPU) {
            this.CPU = CPU;
            return this;
        }

        public Builder setRAM(int RAM) {
            this.RAM = RAM;
            return this;
        }

        public Builder setStorage(int storage) {
            this.storage = storage;
            return this;
        }

        public Builder setGraphicsCard(boolean hasGraphicsCard) {
            this.hasGraphicsCard = hasGraphicsCard;
            return this;
        }

        public Computer build() {
            return new Computer(this);
        }
    }
}
```

#### 使用 Builder：
```java
public class Main {
    public static void main(String[] args) {
        Computer gamingPC = new Computer.Builder()
            .setCPU("Intel i9")
            .setRAM(32)
            .setStorage(1024)
            .setGraphicsCard(true)
            .build();

        Computer officePC = new Computer.Builder()
            .setCPU("Intel i5")
            .setRAM(16)
            .setStorage(512)
            .setGraphicsCard(false)
            .build();

        System.out.println(gamingPC);
        System.out.println(officePC);
    }
}
```

---

## **3. 差異比較**

| **特性**           | **Factory Pattern**                                                   | **Builder Pattern**                                               |
|---------------------|----------------------------------------------------------------------|-------------------------------------------------------------------|
| **目的**            | 隱藏實例化邏輯，根據條件返回不同類型的物件                          | 用於構建屬性多且複雜的物件，支持逐步構建                          |
| **物件複雜度**      | 通常適用於簡單或中等複雜度的物件                                    | 適用於結構複雜的物件                                              |
| **方法數量**        | 使用單一工廠方法或多個靜態方法                                       | 提供多個設定方法與 `build()` 方法                                 |
| **靈活性**          | 可隨參數變化返回不同的類別                                           | 支援屬性逐步構建與靈活配置                                       |
| **實例化時機**      | 一次性產生完整物件                                                  | 可以分步完成物件的構建                                            |
| **例子**            | 形狀（圓形、方形）的選擇                                            | 電腦配置（CPU、RAM、硬碟）的定制化                               |

---

這樣的整理是否符合需求？需要補充其他例子嗎？

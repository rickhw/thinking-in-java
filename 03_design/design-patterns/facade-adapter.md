以下是 **Facade Pattern** 和 **Adapter Pattern** 的特性、範例與差異：

---

## **1. Facade Pattern**

### **特性**
- **目的**：提供一個簡化的介面來隱藏子系統的複雜性，讓客戶端能夠輕鬆使用。
- **核心思想**：定義一個高層介面，對多個子系統進行封裝，簡化客戶端的操作。
- **適用場景**：
  - 子系統包含多個複雜的模組或類別。
  - 希望提供一個更易用的統一介面給客戶端。
  - 減少客戶端與子系統之間的耦合。

### **優缺點**
- **優點**：
  - 簡化系統的使用，降低學習成本。
  - 客戶端與子系統解耦。
- **缺點**：
  - 新增或變更功能時可能需要修改 Facade。

### **Java 範例**

#### 複雜的子系統：
```java
class VideoDecoder {
    public void decode(String video) {
        System.out.println("Decoding video: " + video);
    }
}

class AudioDecoder {
    public void decode(String audio) {
        System.out.println("Decoding audio: " + audio);
    }
}

class SubtitleRenderer {
    public void render(String subtitle) {
        System.out.println("Rendering subtitles: " + subtitle);
    }
}
```

#### Facade 類別：
```java
public class MediaPlayerFacade {
    private VideoDecoder videoDecoder = new VideoDecoder();
    private AudioDecoder audioDecoder = new AudioDecoder();
    private SubtitleRenderer subtitleRenderer = new SubtitleRenderer();

    public void playMedia(String video, String audio, String subtitle) {
        videoDecoder.decode(video);
        audioDecoder.decode(audio);
        subtitleRenderer.render(subtitle);
        System.out.println("Playing media.");
    }
}
```

#### 使用 Facade：
```java
public class Main {
    public static void main(String[] args) {
        MediaPlayerFacade mediaPlayer = new MediaPlayerFacade();
        mediaPlayer.playMedia("video.mp4", "audio.mp3", "subtitles.srt");
    }
}
```

---

## **2. Adapter Pattern**

### **特性**
- **目的**：將現有的類別或介面適配成客戶端需要的另一種介面。
- **核心思想**：透過一個適配器類別，讓不相容的類別能夠一起工作。
- **適用場景**：
  - 現有類別的介面不符合需求，但無法修改原始類別。
  - 整合舊系統和新系統。

### **優缺點**
- **優點**：
  - 增加現有類別的重用性。
  - 客戶端與原有類別解耦。
- **缺點**：
  - 增加系統的複雜度。

### **Java 範例**

#### 現有類別：
```java
class OldCharger {
    public void connectToOldPort() {
        System.out.println("Charging via old port.");
    }
}
```

#### 客戶端需要的新介面：
```java
interface NewCharger {
    void connectToUSBTypeC();
}
```

#### Adapter 類別：
```java
public class ChargerAdapter implements NewCharger {
    private OldCharger oldCharger;

    public ChargerAdapter(OldCharger oldCharger) {
        this.oldCharger = oldCharger;
    }

    @Override
    public void connectToUSBTypeC() {
        System.out.println("Adapter converts Type-C to old port.");
        oldCharger.connectToOldPort();
    }
}
```

#### 使用 Adapter：
```java
public class Main {
    public static void main(String[] args) {
        OldCharger oldCharger = new OldCharger();
        NewCharger adapter = new ChargerAdapter(oldCharger);
        adapter.connectToUSBTypeC();
    }
}
```

---

## **3. 差異比較**

| **特性**           | **Facade Pattern**                                           | **Adapter Pattern**                                              |
|---------------------|-------------------------------------------------------------|------------------------------------------------------------------|
| **目的**            | 提供簡單的介面以隱藏子系統的複雜性                          | 使不相容的介面能夠協同工作                                      |
| **對象**            | 簡化對一組子系統的訪問                                       | 讓現有類別符合新的介面需求                                       |
| **改變方向**        | 不改變子系統，只是包裝一層高層介面                           | 轉換現有類別的介面                                               |
| **使用時機**        | 客戶端需要操作多個子系統時                                   | 客戶端需要與舊系統或不相容類別交互時                            |
| **例子**            | MediaPlayer 使用多個解碼器                                  | 將舊充電器適配為支持 Type-C 的介面                              |

---

這樣的整理是否符合需求？需要補充其他範例或進一步說明嗎？
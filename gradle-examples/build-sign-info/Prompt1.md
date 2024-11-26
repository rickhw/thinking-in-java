我想用 java17, springboot 3.2, gradle (groovy), 讓 gradle 在 build 的過程，可以 sign 資訊到 src/main/resource/version.properties 檔案裡，這個檔案預設內容如下：

```
build.githash=@@GIT_HASH@@
build.timestamp=@@BUILD_TIMESTAMP@@
build.os.name=Linux
build.os.version=6.8.0-49-generic
build.java.version=17.0.13
build.java.provider=Corretto
```

滿足以下條件：

1. 提供一個 api, 可以取得這個 build version 資訊
2. 在啟動 app 的時候，在 console 顯示版本資訊
3. gradle 在 build 的時候，不會改動 src/main/resource/version.properties
4. gradle build 出來的檔案名稱為 <APP_NAME>-<version>-<timestamp>.jar

當我透過 gradle build 之後，如果執行產出的 java -jar build/jars/<APP_NAME>-<version>-<timestamp>.jar，跑起來 Console 會顯示 sign 過的資訊，像是:

```bash
========================================
🚀 Application Startup Information 🚀
========================================
Application Name: my-awesome-application
Build Timestamp: 20241126_0942
Git Hash: 1b56936a8c4d37552bc4409fe4fac7fc9c4eb365
========================================
```

打 /api/version，則會取得以下

```json
{
    "githash": "1b56936",
    "timestamp": "20241126-10:35:14",
    "os.name": "Linux",
    "os.version": "6.8.0-49-generic",
    "java.version": "17.0.13",
    "java.provider": "Corretto"
}
```


如果我直接用 gradle bootRun 則會顯示預設 version.properties 裡的資訊，預設如下：

```bash
========================================
🚀 Application Startup Information 🚀
========================================
Application Name: my-awesome-application
Build Timestamp: @@BUILD_TIMESTAMP@@
Git Hash: @@GIT_HASH@@
========================================
```



請提供完整的程式碼。


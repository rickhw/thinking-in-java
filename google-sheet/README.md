
- [ ] select a table
- [ ] single record

---
## API

- /db/{db-name}
- /db/{db-name}/tables
- /db/{db-name}/tables/{table-name}



---

```bash
gradle init --type basic

mkdir -p src/main/java src/main/resources 
```






---
## 準備

- Google 官方文件提供的範例 Sheet 內容: [點我](https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit#gid=0)
    - SheetId: `1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms`



---


```bash
❯ gradle run

> Task :run
Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=72367673822-36d15cmmf1q4shkba6ph50il8rs3t2mb.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets.readonly
2024-04-11 19:08:26.409 java[62350:1883217] WARNING: Secure coding is not enabled for restorable state! Enable secure coding by implementing NSApplicationDelegate.applicationSupportsSecureRestorableState: and returning YES.
Attempting to open that address in the default browser now...
<=========----> 75% EXECUTING [44s]



❯ gradle run

> Task :run
Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=72367673822-36d15cmmf1q4shkba6ph50il8rs3t2mb.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets.readonly
2024-04-11 19:11:12.584 java[63044:1887615] WARNING: Secure coding is not enabled for restorable state! Enable secure coding by implementing NSApplicationDelegate.applicationSupportsSecureRestorableState: and returning YES.
Attempting to open that address in the default browser now...
<=========----> 75% EXECUTING [20s]

```


成功


```bash
❯ gradle run

> Task :run
Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=72367673822-36d15cmmf1q4shkba6ph50il8rs3t2mb.apps.googleusercontent.com&redirect_uri=http://localhost:8888/Callback&response_type=code&scope=https://www.googleapis.com/auth/spreadsheets.readonly
2024-04-11 19:12:58.344 java[63480:1890454] WARNING: Secure coding is not enabled for restorable state! Enable secure coding by implementing NSApplicationDelegate.applicationSupportsSecureRestorableState: and returning YES.
Attempting to open that address in the default browser now...
Name, Major
Alexandra, English
Andrew, Math
Anna, English
Becky, Art
Benjamin, English
Carl, Art
Carrie, English
Dorothy, Math
Dylan, Math
Edward, English
Ellen, Physics
Fiona, Art
John, Physics
Jonathan, Math
Joseph, English
Josephine, Math
Karen, English
Kevin, Physics
Lisa, Art
Mary, Physics
Maureen, Physics
Nick, Art
Olivia, Physics
Pamela, Math
Patrick, Art
Robert, English
Sean, Physics
Stacy, Math
Thomas, Art
Will, Math

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.3/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 19s
3 actionable tasks: 1 executed, 2 up-to-date
```



---

## Reference

- https://developers.google.com/sheets/api/quickstart/java
- [Source Code](https://github.com/googleworkspace/java-samples/blob/main/sheets/quickstart/src/main/java/SheetsQuickstart.java)
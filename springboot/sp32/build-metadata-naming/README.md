

## Design Principle

- 不使用 pom.xml 的資訊
- 資訊由外部環境變數注入
- 輸出的 artifact image 命名規則與 API 提供的必須一致


Artifact Naming:

> <artifactId>-<version>.jar

for example:

> booter-service-0.2.0-SNAPSHOT.jar


## Debugging time

```json
{
"groupId:": "@GROUP_ID@",
"roleId:": "@ROLE_ID@",
"version:": "@VERSION@",
"buildType:": "default_build_type",
"buildId:": "yyyyMMdd-HHmm",
"hashcode:": "default_hashcode"
}
```


## Runtime

```json
{
"groupId:": "rws",
"roleId:": "booter",
"version:": "0.2.0",
"buildType:": "dev",
"buildId:": "20240525-0935",
"hashcode:": "00f76d2c8004126a053497fe3f78c9a90155d174"
}
```



## 顯示哪些支援的 template

支援很多，分成幾大類：

1. Client
2. Server
3. Documentation
4. Schema
5. Config

```bash
❯ openapi-generator list
The following generators are available:

CLIENT generators:
    - ada
    - android
    - bash
    - c

SERVER generators:
    - go-server

    - java-camel
    - java-inflector
    - java-play-framework
    - jaxrs-cxf
    - kotlin-server
    - php-laravel
    - python-fastapi (beta)
    - python-flask
    - ruby-on-rails
    - rust-server
    - spring

DOCUMENTATION generators:
    - asciidoc
    - cwiki
    - dynamic-html
    - html
    - html2
    - markdown (beta)
    - openapi
    - openapi-yaml
    - plantuml (beta)

SCHEMA generators:
    - graphql-schema
    - mysql-schema
    - postman-collection (beta)
    - protobuf-schema (beta)
    - wsdl-schema (beta)

CONFIG generators:
    - apache2
```


## 產生 Java SpringBoot (2.7) 

```bash
openapi-generator generate \
     -i openapi-petstore.yaml \
     -g spring \
     -o out
```


## 查詢 generator spring 支援哪一些 Config

```bash
openapi-generator config-help -g spring > spring.md
```

透過 config 的方式，指定 generator `spring` 更細節的參數：

```bash
openapi-generator generate \
     -i openapi-petstore.yaml \
     -g spring \
     -c spring-config.yaml \
     -o out
```

spring-confi.yaml 內容如下：


```yaml
basePackage: com.abc.pet.codegen
modelPackage: com.abc.pet.codegen.model
apiPackage: com.abc.pet.codegen.api
```





---

## Templates

取得 generator 的 templates
```bash
openapi-generator author template -g spring
```



```bash
openapi-generator generate \
     -i openapi-petstore.yaml \
     -g spring \
     -c spring-config.yaml \
     -t codegen/templates \
     -o out
```

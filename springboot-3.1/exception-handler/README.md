
# day01

## Validation

- request payload
    - [v] single layer payload
        - string:
            - pattern
            - email
            - url
        - integer
        - object
            - date
            - array
    - [ ] nest payload
        - [ ] abstract / interface DTO
    - [v] validate json payload
        - https://www.codejava.net/frameworks/spring-boot/rest-api-request-validation-examples
    - [ ] query string
        - https://www.codejava.net/frameworks/spring-boot/rest-api-validate-query-parameters-examples
    - [ ] path parameters
        - https://www.codejava.net/frameworks/spring-boot/rest-api-validate-path-variables-examples
    - [v] http headers
- [v] validation handler per API
- [v] validation handler by global handler
- [v] unit test
- response payload




## Validate Path Variable (Parameter)

- Springboot does not support @Valid on @PathVariable


## API with Dry-Run function








---

## Reference

- https://springexamples.com/spring-boot-request-validation/
- https://www.bezkoder.com/spring-boot-validate-request-body/
- https://www.appsdeveloperblog.com/read-json-request-body-in-spring-web-mvc/
- [Spring Boot Global Exception Handler Examples](https://www.codejava.net/frameworks/spring-boot/global-exception-handler-examples)
- [Spring Boot REST API Validate Path Variables Examples](https://www.codejava.net/frameworks/spring-boot/rest-api-validate-path-variables-examples)
- [Validate Request Body in Spring Boot](https://www.bezkoder.com/spring-boot-validate-request-body/)
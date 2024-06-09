
- Gradle: 8.3, 8.7
- Java 17 (OpenJDK)
- Spring Boot 2.7.3


---

## Simulator

* * * * * /home/ubuntu/simulator.sh <target_host>


---

## Metric by Log


The log format:

```json
{ "timestamp":"2024-06-09T00:16:03.922", "requestId": "59c4762b-9cbe-4fac-97a5-40017d54a4bb", "level":"INFO", "class":"c.g.race.controller.RootController", "thread":"http-nio-8092-exec-10", "value": "-18064", "message": "operate(), value is -18064" }
```


JQL for value fields:

the field must be defined as label.

```sql
avg_over_time({host="ip-172-31-5-186"} | unwrap value [1m])  -- work
avg_over_time({host="ip-172-31-5-186"}  |= "capacityUnit" | json | unwrap value [1m])
avg_over_time({host="ip-172-31-5-186"}  |= "capacityUnit" | unwrap capacityUnit [1m])
avg_over_time({host="ip-172-31-5-186"}  |= "consumedValue" | unwrap consumedValue [1m])

sum_over_time({host="ip-172-31-5-186"} |= "capacityUnit"
| json
| unwrap capacityUnit [1m])

```


JQL for sum of request:

```sql
sum(count_over_time({host="ip-172-31-5-186"} |= "requestId" [1m]))
```



---
## ref

- https://piotrminkowski.com/2023/07/05/logging-in-spring-boot-with-loki/


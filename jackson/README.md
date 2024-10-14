
1. Marshell: Nest Model to JSON
2. Unmarshell: JSON to Model


```bash
~/repos/thinking-in-java/jackson$ gradle clean build

Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.10.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 25s
10 actionable tasks: 10 executed


~/repos/thinking-in-java/jackson$ ls build/libs/
jackson-1.0-SNAPSHOT-all.jar  jackson-1.0-SNAPSHOT.jar


~/repos/thinking-in-java/jackson$ java -jar build/libs/jackson-1.0-SNAPSHOT-all.jar
Converted from JSON: John Doe, Anytown
Converted to JSON: {"name":"John Doe","age":30,"address":{"street":"123 Main St","city":"Anytown"}}

```



mvn archetype:generate \
	-DgroupId=com.gtcafe.lab \
	-DartifactId=mustache-lab \
	-DarchetypeArtifactId=maven-archetype-quickstart \
	-DinteractiveMode=false


mvn clean install


com.gtcafe.lab.Main

---

## reference

- https://www.baeldung.com/mustache
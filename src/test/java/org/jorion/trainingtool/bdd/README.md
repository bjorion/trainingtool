
# BDD

* Behavior Driven Development
* Execute `maven clean verify` and find the rapport in `target/site/serenity/index.html`
* https://serenity-bdd.github.io/theserenitybook/latest/index.html
* https://en.wikipedia.org/wiki/Behavior-driven_development

### POM dependencies

(For the Serenity framework)

```xml
	<dependency>
		<groupId>net.serenity-bdd</groupId>
		<artifactId>serenity-junit</artifactId>
		<version>${serenity.version}</version>
		<scope>test</scope>
	</dependency>
	
	<dependency>
		<groupId>net.serenity-bdd</groupId>
		<artifactId>serenity-screenplay</artifactId>
		<version>${serenity.version}</version>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>org.assertj</groupId>
		<artifactId>assertj-core</artifactId>
		<version>3.6.2</version>
		<scope>test</scope>
	</dependency>

	<dependency>
		<groupId>org.hamcrest</groupId>
		<artifactId>hamcrest-all</artifactId>
		<version>1.3</version>
		<scope>test</scope>
	</dependency>
	
	...
	
	<plugin>
		<groupId>net.serenity-bdd.maven.plugins</groupId>
		<artifactId>serenity-maven-plugin</artifactId>
		<version>${serenity.maven.version}</version>
		<configuration>
			<tags>${tags}</tags>
		</configuration>
		<executions>
			<execution>
				<id>serenity-reports</id>
				<phase>post-integration-test</phase>
				<goals>
					<goal>aggregate</goal>
				</goals>
			</execution>
		</executions>
	</plugin>	
```

### Glossary

* Feature = package
* Story = class
* Scenario = method


* GIVEN = initial situation
* WHEN  = action
* THEN  = result


### Annotations

**Unit class**
* @RunWith(SerenityRunner.class)
* @Narrative(text={"Explain what the class does"})

**Unit methods**
* @Test
* @Title("")
* @Pending

**Step library**
* @Steps: A Serenity step library = model the behavior of our users. Instantiated automatically by Serenity.
* @Shared == @Steps(shared = true)
* @Step: a step in a step library
* actor: optional field that contains the name of the variable
* @Step("#actor starts with {0}")

**Serenity Classes**
* Performable: a task or action that can be performed by an actor






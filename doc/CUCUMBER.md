# Cucumber

1. [Cucumber Reference Site](https://cucumber.io/docs/guides/)
1. [Cucumber with Spring](https://github.com/cucumber/cucumber-jvm/tree/main/spring) (Reference Site)
1. [Cucumber with Spring](https://www.baeldung.com/cucumber-spring-integration) (Baeldung)
1. [Cucumber with Spring Boot](https://thepracticaldeveloper.com/cucumber-tests-spring-boot-dependency-injection/)


### pom.xml

DependencyManagement:

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>org.junit</groupId>
				<artifactId>junit-bom</artifactId>
				<version>${junit-bom.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<dependency>
				<groupId>io.cucumber</groupId>
				<artifactId>cucumber-bom</artifactId>
				<version>${cucumber.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>

Dependencies:

	<dependencies>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-java</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-junit-platform-engine</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-spring</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.junit.platform</groupId>
			<artifactId>junit-platform-suite</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.junit.jupiter</groupId>
			<artifactId>junit-jupiter</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>


### Test files

* Add **src/test/java/.../cucumber/TestRunner.java**, check value of **@SelectClasspathResource(fullpath)** (see below)
* Add **src/test/resources/cucumber.properties** and **...junit-platform.properties** with `cucumber.publish.quiet=true`
* Add **src/test/resources/.../cucumber/<file>.feature** with feature (scenarios)
* Execute `mvn test` to have a skeleton of the methods to write
* Complete **src/test/java/.../cucumber/StepDefinitions.java** with the methods



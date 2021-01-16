
## Documentation

* https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api
* https://www.baeldung.com/spring-boot-rest-client-swagger-codegen
* https://springfox.github.io/springfox/docs/current/

## Steps by Steps

* **Swagger** (v2): download swagger-codegen-cli-$version.jar 
* URL: https://mvnrepository.com/artifact/io.swagger/swagger-codegen-cli
* **OpenApi** (v3): download openapi-codegen-cli-$version.jar 
* URL: https://mvnrepository.com/artifact/org.openapitools/openapi-generator-cli


* Download api-docs.json from http://localhost:8080/trainingtool/swagger-ui/
* Rename it **v2.api-docs.json** or **v3.api-docs.json** if you are using v2 or v3
* v3: Replace «User» by User and «StatDTO» by StatDTO in api-docs.json
* Define a environment parameter JAVA8_HOME that points to Java 8


* Run **gen-swagger-client.bat** to generate a client project **trainingtool-rest-swagger**
* Run **gen-openapi-client.bat** to generate a client project **trainingtool-rest-openapi**

**In the generated project**

* Set the java version to 8: **<source>8</source>**

```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<version>3.6.1</version>
	<configuration>
		<source>8</source>
		<target>8</target>
	</configuration>
</plugin>
```

* Add the version to the maven-javadoc-plugin: **<source>8</source>**

```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-javadoc-plugin</artifactId>
	<version>...</version>
	<configuration>
		<source>8</source>
		<doclint>none</doclint>
	</configuration>
	...
<plugin>
```

* v2: you may have to add the dependency below to the project if Maven is using Java 11 or above

```
<dependency>
	<groupId>jakarta.annotation</groupId>
	<artifactId>jakarta.annotation-api</artifactId>
	<version>1.3.5</version>
</dependency>
```
				
* Compile the client project
* cURL example:

```
curl -X GET "http://localhost:8080/trainingtool/REST/v1/regs" -H  "accept: application/json" -H  "authorization: Basic cmVzdHN5c3RlbTpyZXN0c3lzdGVt"`
```

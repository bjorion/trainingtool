## Documentation

.

## Create a client project

* https://www.baeldung.com/java-openapi-generator-server
* Download and save the apidoc JSON file
* Maven POM

```xml
<project>
<dependencies>
    <dependency>
        <groupId>io.swagger.core.v3</groupId>
        <artifactId>swagger-annotations</artifactId>
        <version>${swagger-annotations-version}</version>
    </dependency>

    <!-- Spring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    
        <plugin>
            <groupId>io.swagger.codegen.v3</groupId>
            <artifactId>swagger-codegen-maven-plugin</artifactId>
            <version>3.0.26</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                    <configuration>
                        <inputSpec>${project.basedir}/src/main/resources/v3.api-docs.json</inputSpec>
                        <language>java</language>
                        <library>resttemplate</library>
                        <configOptions>
                            <sourceFolder>src/gen/java/main</sourceFolder>
                            <apiPackage>${default.package}.api</apiPackage>
                            <modelPackage>${default.package}.model</modelPackage>
                            <invokerPackage>${default.package}.handler</invokerPackage>
                            <dateLibrary>java8</dateLibrary>
                        </configOptions>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
</project>
```

* Configuration class example (using basic authentication)

```java

@Configuration
public class ClientConfig {
    @Bean
    public WsControllerApi wsControllerApi() {
        return new WsControllerApi(apiClient());
    }

    @Bean
    public ApiClient apiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("http://localhost:8080/trainingtool");

        HttpBasicAuth auth = (HttpBasicAuth) apiClient.getAuthentication("authBasic");
        auth.setUsername("YOUR USERNAME");
        auth.setPassword("YOUR PASSWORD");
        return apiClient;
    }
}
```

# Create a server project

* https://www.baeldung.com/java-openapi-generator-server

```xml

<dependency>
    <groupId>javax.validation</groupId>
    <artifactId>validation-api</artifactId>
</dependency>

<dependency>
<groupId>org.openapitools</groupId>
<artifactId>jackson-databind-nullable</artifactId>
<version>0.2.1</version>
</dependency>

<dependency>
<groupId>io.springfox</groupId>
<artifactId>springfox-swagger2</artifactId>
<version>2.10.5</version>
</dependency>


<plugin>
<groupId>org.openapitools</groupId>
<artifactId>openapi-generator-maven-plugin</artifactId>
<version>5.1.1</version>
<executions>
    <execution>
        <goals>
            <goal>generate</goal>
        </goals>
        <configuration>
            <inputSpec>
                ${project.basedir}/src/main/resources/v3.api-docs.json
            </inputSpec>
            <generatorName>spring</generatorName>
            <apiPackage>${default.package}.api</apiPackage>
            <modelPackage>${default.package}.model</modelPackage>
            <supportingFilesToGenerate>
                ApiUtil.java
            </supportingFilesToGenerate>
            <configOptions>
                <delegatePattern>true</delegatePattern>
            </configOptions>
        </configuration>
    </execution>
</executions>
</plugin>
 ```
 
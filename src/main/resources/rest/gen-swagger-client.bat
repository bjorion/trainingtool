
@ECHO OFF
SET SWAGGER_JAR=swagger-codegen-cli-2.4.17.jar

ECHO To list all Java-related options, type the command:
ECHO java -jar swagger-codegen-cli-2.4.17.jar config-help -g java
ECHO java -jar swagger-codegen-cli-2.4.17.jar help generate
ECHO.

SET CLIENT_NAME=trainingtool-rest-swagger
SET CLIENT_VERSION=0.9.0-SNAPSHOT
SET CLIENT_GROUP=org.jorion
SET CLIENT_PACKAGE=org.jorion.trainingtool.rest.swagger
SET JAVA_PATH=%JAVA8_HOME%\bin

"%JAVA_PATH%\java" -jar %SWAGGER_JAR% generate ^
  -i v2.api-docs.json ^
  -l java ^
  --api-package %CLIENT_PACKAGE%.api ^
  --model-package %CLIENT_PACKAGE%.model ^
  --invoker-package %CLIENT_PACKAGE%.invoker ^
  --group-id org.jorion ^
  --artifact-id %CLIENT_NAME% ^
  --artifact-version %CLIENT_VERSION% ^
  --library resttemplate ^
  -o ../../../../../%CLIENT_NAME%
  

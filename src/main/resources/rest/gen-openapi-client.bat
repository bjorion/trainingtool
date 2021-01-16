
@ECHO OFF
SET OPENAPI_JAR=openapi-generator-cli-5.0.0.jar

ECHO To list all Java-related options, type the command:
ECHO java -jar openapi-generator-cli-5.0.0.jar config-help -g java
ECHO java -jar openapi-generator-cli-5.0.0.jar help generate
ECHO.

SET CLIENT_NAME=trainingtool-rest-openapi
SET CLIENT_VERSION=0.9.0-SNAPSHOT
SET CLIENT_GROUP=org.jorion
SET CLIENT_PACKAGE=org.jorion.trainingtool.rest.openapi
SET JAVA_PATH=%JAVA8_HOME%\bin

"%JAVA_PATH%\java" -jar %OPENAPI_JAR% generate ^
  -i v3.api-docs.json ^
  -g java ^
  --api-package %CLIENT_PACKAGE%.api ^
  --model-package %CLIENT_PACKAGE%.model ^
  --invoker-package %CLIENT_PACKAGE%.invoker ^
  --group-id org.jorion ^
  --artifact-id %CLIENT_NAME% ^
  --artifact-version %CLIENT_VERSION% ^
  --library resttemplate ^
  -o ../../../../../%CLIENT_NAME%
  

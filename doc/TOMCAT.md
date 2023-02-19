# Tomcat

Information on how to deploy this application on Tomcat.

### Start the application with the embedded Tomcat

* This application can be run as a stand-alone. You do not need any server to host it. It comes with a light version of tomcat included.
* The starting class is `org.jorion.trainingtool.TrainingToolApp` 
* To start the application within Eclipse, right-click on the `TrainingToolApp` class and select `Run As > Java Application`.
* You can start the application in the Debug mode by selecting `Debug As > Java Application` instead.
* Information will be displayed in the console. You should see the line `Tomcat started on port(s): 8080 (http)`.
* The application port (8080) is configured in the file `/config/application.properties`
* You can generate a war file with the command `mvn clean install`. You will find the file in the `target` folder: `trainingtool-<version>.war`
* You can run the application outside Eclipse with the command `mvn spring-boot:run` or `java -jar target\trainingtool-<version>.war` from your project folder.
* **Important**: to use the H2 DB, start the application with the VM arguments `-Dspring.profiles.active=local` 
* Example: `java -Dspring.profiles.active=local -jar target\trainingtool-<version>.war`

.

### Start the application with an external Tomcat

* Create a new file called `setenv.bat` in TOMCAT_HOME\bin (or `setenv.sh` in a *nix system)
* In this file, define a new environment variable: `SET TRAININGTOOL_HOME=%HOMEPATH%`
* Instead of %HOMEPATH%, you can use any variable corresponding to a folder that you (and Tomcat) have access to
* In this folder, create a new file called `trainingtool.properties`
* Copy the properties from `config\application.properties` to that new file
* Generate the war file: `trainingtool-<version>.war`
* In Tomcat, go to the management page (you'll need to set up a user for this)
* Upload and deploy the war file

.

### Start the application with a Tomcat as a Service

* Create a file `trainingtool.xml` under `tomcat/conf/Catalina/trainingtool/`
* Content of the file:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <Environment name="spring.config.location" value="r:/trainingtool/config/" type="java.lang.String"/>
</Context>
```

* Create an **application.properties** file in the folder `r:/trainingtool/config/`
* That file must contain the following properties: 
    - username/password for DB (**spring.datasource.username, spring.datasource.password**)
    - password for the SMTP server (**spring.mail.password**)
    - the active profile (**spring.profiles.active**=tst or prd)
* Source: https://stackoverflow.com/questions/41461786/how-to-externalize-application-properties-in-tomcat-webserver-for-spring

.

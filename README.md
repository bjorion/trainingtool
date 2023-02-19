# Training Tool Application

### Vocabulary

**People**

* **User**: the person currently logged in, whatever his role
* **Member**: the person taking the training, normally a simple employee
* **Manager**: any member with people reporting to him
* **HR**: the management who, under some conditions, has to approve the request after the manager
* **Training Team**: a member of the Training team who has to approve the request and contact the provider for the actual registration
* **Admin**: a technical user who can see all requests
* **Supervisor**: manager and above (Manager, HR, Training Team, Admin)  
* **Office**: HR and above (HR, Training Team, Admin)

Note that the members of the HR or TRAINING TEAM groups are defined in the properties files.

**Business Entities**

* **User**: a user logged in the application, or a member with a registration associated; source = LDAP
* **Training**: a predefined template for a cursus; only office people can create or edit them
* **Registration**: a request to follow a training, predefined or not; usually taken by an employee


### How to build the application

Do you have compilation issues in Eclipse ? Check that Lombok and MapStruct are corrected installed (see **Technical information** below).
Anyway, you should always be able to compile via Maven.

* `mvn clean install` to generate the archive (a war file)
* `mvn site` to generate the HTML documentation
* `mvn javadoc:javadoc` to generate the api javadoc

(note: there may be some issues in function of the java version you're using)


### How to run the application

* With Eclipse, make sure the profile **local** is selected (pom > Maven > Select Maven Profile, select "local"). This will add the H2 dependency.
* The application runs with an embedded tomcat. See [Tomcat](./doc/TOMCAT.md)


### How to access the application

* LOCAL: http://localhost:8080/trainingtool/
* TEST: be-cv-app-test.resultinfra.com:83/trainingtool/
* PROD: https://be-cvengine.resultinfra.com/trainingtool/login


* You can log in using a pre-defined user (see below) if the flag `app.users.inMemory` is true
* You can also log in by giving your username and password if the flag `app.users.ldap` is true


### How to login with pre-defined users

* MEMBER: john.doe/john
* MANAGER: manager/manager
* HR: hr/hr
* TRAINING TEAM: training/training


## How to release the application (TEST/PROD)

* You need the corresponding access rights
* In `application-prd.properties`, be sure to update the value for `spring.datasource.password`
* (to be completed...)


# Technical Information

* [Actuator](.doc/ACTUATOR.md)
* [DB](./doc/DB.md)
* [GIT](./doc/GIT.md)
* [LDAP](./doc/LDAP.md)
* [Lombok](./doc/LOMBOK.md)
* [Mail](./doc/MAIL.md)
* [MapStruct](./doc/MAPSTRUCT.md)
* [REST](./doc/REST.md) 
* [Tomcat](./doc/TOMCAT.md)


### Idea for future developments

* Add filtering / sorting to the registration list
* Use internal ApacheMQ for the emails (in case the mail server is down)
* investigate jira/jenkins

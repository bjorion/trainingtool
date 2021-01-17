# Training Tool Application

### Start the application

* java -Dspring.profiles.active=local -jar trainingtool-1.0.0.war

.

### Vocabulary

* **User**: the person currently logged in, whatever his role
* **Member**: the person taking the training, normally a simple employee
* **Manager**: any member with people reporting to him
* **HR**: the management who, under some conditions, has to approve the request after the manager
* **Training**: a member of the Training team who has to approve the request and contact the provider for the actual registration
* **Admin**: a technical user who can see all requests
* **Supervisor**: manager and above (Manager, HR, Training, Admin)  
* **Office**: HR and above (HR, Training, Admin)

.

### Access the application

* Start the application by going to [http://localhost:8080/trainingtool/](http://localhost:8080/trainingtool/)
* You can log in using a pre-defined user (see below) if the flag `app.users.inmemory` is true
* You can also log in by giving your username and password if the flag `app.users.ldap` is true
* Use `mvn site` to generate the site, or `mvn javadoc:javadoc` to generate the api javadoc

.

### How to login with pre-defined users

* MEMBER: jdoe/john
* MANAGER: manager/manager
* HR: hr/hr
* TRAINING: training/training
* ADMIN: admin/admin

.

# Technical Information

### Miscellaneous

* By default, the **PostgreSQL** DB is used. To use H2 instead, set the active spring profile to `local`. 
* The `h2-schema.sql` and `h2-data.sql` files are read by Spring to initialize the H2 DB.
* You can control the logging level of the application by modifying the file `src/main/resources/logback.xml`

.

### Spring Actuator

* HEALTH: http://localhost:8080/trainingtool/actuator/health
* INFO  : http://localhost:8080/trainingtool/actuator/info

Other endpoints are available for admins

.

### Springfox

* http://localhost:8080/trainingtool/swagger-ui

.

### REST Endpoints

* http://localhost:8080/trainingtool/REST/v1/users
* http://localhost:8080/trainingtool/REST/v1/regs

.

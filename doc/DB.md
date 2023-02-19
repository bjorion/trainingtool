# DB

Information on the Database

* By default, the **PostgreSQL** DB is used. To use H2 instead, set the active spring profile to `local`. 
* The `h2-schema.sql` and `h2-data.sql` files are read by Spring to initialize the H2 DB.
* Locally, you can access the H2 console via the link `http://localhost:8080/trainingtool/h2-console/`
* SQL files are stored under src/main/resources/dbcr
* 2nd level cache: https://docs.jboss.org/hibernate/orm/5.2/userguide/html_single/chapters/caching/Caching.html
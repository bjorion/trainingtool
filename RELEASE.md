# Releases

### v0.10.0 (2021-05-14)

- update spring boot (to 2.5.0)
- use junit-jupiter for the tests (replace junit 4)
- refactoring the properties files
- use lombok
- mapper

### v1.0.0

- stable release

### v1.1.0

- specific mail sent to the employee when a request is approved by the Provider
- a refused training can be deleted by the employee
- requests already approved by the Provider are still editable by the training team
- a manager sees only requests for himself or his employees, not requests from other manager users

### v1.2.0

- update spring boot (to 2.5.2)
- creation / edit / removal of trainings (by office)
- possility to create a registration based on an existing training
- caching images
- UI improved (layout standardized on all pages)
- more use of "redirect" between pages
- manager is able to bulk-assign registration using a training
- list of upcoming trainings
- 2nd level cached added for trainings

### v1.2.1 (2021-08-30)

- update spring boot (to 2.5.3)
- username in login page set to lowercase before being sent to the server (avoid issues with LDAP)
- refactoring: use of "userName" and "managerName" made more consistent
- page 404 added, page 403 refactored
- bugfix: if an HR person was also manager, he could not see registrations in the state 'SUBMITTED_TO_HR'

### v1.2.2 (not released)

- update spring boot (to 2.5.4)
- bugfix: the login page (show password + check on IE)
- bugfix: the status page (username replaced by userName)
- bugfix: restrict HR visibility on registrations to SUBMITTED_TO_HR + SUBMITTED_TO_MANAGER (if relevant)

### v1.3.0 (2021-11-20)

- update spring boot (to 2.5.6) 
- 'motivation' renamed 'justification' (except for the field in the DB)
- field "justify refusal" added when refusing a registration
- bugfix: restrict manager definition (previous definition was too wide)
- bugfix: justification deleted after an edit (by a member)
- button 'Cancel' renamed 'Home' if it redirects to the home page

### v1.3.1 (2022-xx-xx)

- update spring boot (to 2.6.2) and other dependencies
- @Param annotation added for queries
- unit tests added (context loader; controllers)
- tt-dbcr-1.3.1.sql

### v1.3.2 (2022-xx-xx)

- update spring boot (to 2.6.11) and other dependencies
- rename firstname/lastname to firstName/lastName for consistency
- @Transactional moved to Service layer
- update login page (todo)

### v1.4.0 (2024-10-28)
- update spring boot (to 3.3.5) and other dependencies
- update thymeleaf

### v1.5.0 (2024-11-xx)
- refactor all packages
- add ArchUnit tests

### v1.6.0 (2025-05-xx)
- upgrade spring boot (to 3.5.0)

### v1.6.1. (2025-08-xx)
- upgrade spring boot (to 3.5.5)
- upgrade spring modulith (1.4.3)

### v1.7.0 (2025-10-xx)
- upgrade to JDK 25

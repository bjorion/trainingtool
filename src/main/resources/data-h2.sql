
-- Users
INSERT INTO myuser (username, pnr, role, firstname, lastname, sector, managername, function)
	VALUES ('admin', 'LPS001', 'MEMBER:ADMIN', 'AdminFirstname', 'AdminLastname',  null, null, 'Application Admin');
INSERT INTO myuser (username, pnr, role, firstname, lastname, sector, managername, function)
	VALUES ('training', 'LPS002', 'MEMBER:TRAINING', 'TrainingFirstname', 'TrainingLastname', null, null, 'Training Team');
-- HR is also manager for MANAGER
INSERT INTO myuser (username, pnr, role, firstname, lastname, sector, managername, function)
	VALUES ('hr', 'LPS003', 'MEMBER:MANAGER:HR', 'HrFirstname', 'HrLastname', null, null, 'HR Team');
INSERT INTO myuser (username, pnr, role, firstname, lastname, sector, phone_number, managername, function)
	VALUES ('manager', 'LPS004', 'MEMBER:MANAGER', 'MgrFirstname', 'MgrLastname', 'Energy & Utilities (E&U)', '+32 (0)475 12 34 56', 'hr', 'DIRECTOR CONSULTING SERVICES');
INSERT INTO myuser (username, pnr, role, firstname, lastname, sector, phone_number, managername, function)
	VALUES ('john.doe', 'LPS005', 'MEMBER', 'John', 'Doe', 'Energy & Utilities (E&U)', '0477123456', 'manager', 'JUNIOR CONSULTANT'); -- id = 5
	
-- Registration
-- for JOHN DOE
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'DRAFT', 5, 'CEVORA', '85073003328', 'Thymeleaf', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, provider_other, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('manager', 'DRAFT', 5, 'OTHER', 'MOOC', 'Kubernetes', 'Open source tool from Google','http://www.kubernetes.org', '2021-11-10', '2021-11-11', 'EVENING', 16, 'CLASSROOM', '1500.00');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'SUBMITTED_TO_MANAGER', 5, 'CEVORA', '85073003328', 'Demo1', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'SUBMITTED_TO_HR', 5, 'CEVORA', '85073003328', 'Demo2', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'SUBMITTED_TO_TRAINING', 5, 'CEVORA', '85073003328', 'Demo3', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'SUBMITTED_TO_PROVIDER', 5, 'CEVORA', '85073003328', 'Demo4', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'REFUSED_BY_HR', 5, 'CEVORA', '85073003328', 'Demo5', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('john.doe', 'REFUSED_BY_PROVIDER', 5, 'CEVORA', '85073003328', 'Demo5', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');

-- for MANAGER
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('manager', 'DRAFT', 4, 'CEVORA', '85073003328', 'Thymeleaf', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');
INSERT INTO registration (created_by, status, user_id, provider, ssin, title, description, url, start_date, end_date, period, total_hour, location, price) 
	VALUES('manager', 'SUBMITTED_TO_MANAGER', 4, 'CEVORA', '85073003328', 'Thymeleaf', 'Thymeleaf replaces JSP','http://www.thymeleaf.org', '2021-11-01', '2021-11-01', 'DAY', 8, 'VIRTUAL', '3.14');

-- Training
INSERT INTO training (created_by, created_on, modified_on, enabled, enabled_from, enabled_until, provider, provider_other, title, description, url, price, start_date, end_date, period, total_hour, location, self_study, motivation)
    VALUES('hr', '2001-01-01', '2001-01-01', true, null, null, 'CEVORA', null, 'Java in 21 Days', 'Everything you wanted to know about java but never dared to ask', 'http://www.javasoft.com', '0', '2022-01-01', '2022-01-21', 'DAY', '12', 'VIRTUAL', 'YES', 'Java is a must know');
INSERT INTO training (created_by, created_on, modified_on, enabled, enabled_from, enabled_until, provider, provider_other, title, description, url, price, start_date, end_date, period, total_hour, location, self_study, motivation)
    VALUES('hr', '2001-01-01', '2001-01-01', true, '2021-07-01', '2021-12-31', 'COURSERA', null, 'Kotlin for Beginners', 'Learn Kotlin By Yourself', 'http://www.kotlin.com', '100.12', '2022-02-01', '2022-02-21', 'DAY', '13', 'VIRTUAL', 'YES', 'kotlin is the next java');
INSERT INTO training (created_by, created_on, modified_on, enabled, enabled_from, enabled_until, provider, provider_other, title, description, url, price, start_date, end_date, period, total_hour, location, self_study, motivation)
    VALUES('hr', '2001-01-01', '2001-01-01', false, '2021-07-01', '2021-12-31', 'OTHER', null, 'Camunda By Example', 'Learn Camunda By Yourself', 'http://www.camunda.com', '200.00', null, null, 'DAY', '14', 'VIRTUAL', 'NO', 'camunda is a hot topic');




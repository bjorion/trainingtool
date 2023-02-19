-- Changes for the version 1.3.1
-- TO CHECK: is it MODIFY or ALTER COLUMN ?

ALTER TABLE trainingtool.registration ALTER COLUMN description varchar(2000); 
ALTER TABLE trainingtool.registration ALTER COLUMN comment  varchar(2000);
ALTER TABLE trainingtool.registration ALTER COLUMN motivation  varchar(2000);

ALTER TABLE trainingtool.training ALTER COLUMN description varchar(2000); 
ALTER TABLE trainingtool.training ALTER COLUMN motivation  varchar(2000); 

-- VARCHAR SIZE:
--   use 50 for enums or numbers
--   use 255 by default
--   use 1000 for free texts

-- drop tables
DROP TABLE trainingtool.user;
DROP TABLE trainingtool.registration;

-- CREATE SCHEMA trainingtool;

-- User
CREATE TABLE IF NOT EXISTS trainingtool.user (
  id SERIAL PRIMARY KEY, 
  version INT DEFAULT 0,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  pnr VARCHAR(255) NOT NULL UNIQUE,
  username VARCHAR(255) NOT NULL UNIQUE,
  role VARCHAR(255) NULL,
  lastname VARCHAR(255) NOT NULL,
  firstname VARCHAR(255) NOT NULL,
  email VARCHAR(255) NULL,
  sector VARCHAR(255) NULL,
  phone_number VARCHAR(50) NULL,
  function VARCHAR(255) NULL,
  managername VARCHAR(255) NULL,
  subco BOOLEAN DEFAULT FALSE,
  ssin VARCHAR(15) NULL -- currently not used
);

-- index
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_username
ON trainingtool.user (username);

-- Registration
CREATE TABLE IF NOT EXISTS trainingtool.registration (
  id SERIAL PRIMARY KEY,
  version INT DEFAULT 0,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255) NOT NULL,
  modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_by VARCHAR(255) NULL,
  status VARCHAR(50) NOT NULL,
  user_id INT NOT NULL, -- FK
  training_id INT NULL, -- FK, currently not used
  
  provider VARCHAR(255) NOT NULL,
  provider_other VARCHAR(255) NULL,
  ssin VARCHAR(15) NULL,  
  title VARCHAR(255) NOT NULL, 
  description VARCHAR(1000) NULL,
  url VARCHAR(1000) NOT NULL,
  price VARCHAR(50) NULL,
  
  start_date TIMESTAMP NULL,
  end_date TIMESTAMP NULL,
  period VARCHAR(50) NOT NULL,
  total_hour VARCHAR(255) NULL,
  location VARCHAR(50) NULL,  
  self_study VARCHAR(50) NULL,
  
  mandatory VARCHAR(50) NULL,
  billable VARCHAR(50) DEFAULT 'NO', 
  cba_compliant VARCHAR(50) NULL,
  
  comment VARCHAR(1000) NULL,
  motivation VARCHAR(1000) NULL,
  
  FOREIGN KEY (user_id) REFERENCES trainingtool.user(id) -- define FK link
);

-- index
CREATE INDEX IF NOT EXISTS idx_registration_status
ON trainingtool.registration (status);


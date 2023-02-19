-- Changes for the version 1.2.0

-- drop tables
DROP TABLE trainingtool.training;

-- New table: Training
CREATE TABLE trainingtool.training (
  id INT AUTO_INCREMENT PRIMARY KEY,
  version INT DEFAULT 0,
  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_by VARCHAR(255) NOT NULL,
  modified_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  modified_by VARCHAR(255) NULL,
  
  -- activation 
  enabled BOOLEAN DEFAULT FALSE,
  enabled_from TIMESTAMP NULL,
  enabled_until TIMESTAMP NULL,
  
  -- registration
  provider VARCHAR(255) NOT NULL,
  provider_other VARCHAR(255) NULL,
  title VARCHAR(255) NOT NULL, 
  description VARCHAR(1000) NOT NULL,
  url VARCHAR(1000) NOT NULL,
  price VARCHAR(50) NULL,
  
  start_date TIMESTAMP NULL,
  end_date TIMESTAMP NULL,
  period VARCHAR(50) NULL,
  total_hour VARCHAR(255) NULL,
  location VARCHAR(50) NULL,
  self_study VARCHAR(50) NULL,
  
  mandatory VARCHAR(50) NULL,
  cba_compliant VARCHAR(50) NULL,
  motivation VARCHAR(1000) NULL
);

-- index
CREATE INDEX idx_training_enabled
ON trainingtool.training (enabled);

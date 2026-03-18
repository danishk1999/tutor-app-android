CREATE DATABASE TutorAppDB;

USE TutorAppDB;

CREATE TABLE Users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  username VARCHAR(50) UNIQUE,
  email VARCHAR(100) UNIQUE,
  password_hash VARCHAR(255),             -- Store hashed passwords
  profile_picture_url VARCHAR(255),        -- Store image URLs for profile pictures
  bio TEXT,                                -- Personal bio
  contact_number VARCHAR(20),
  role ENUM('student', 'tutor', 'admin'),  -- User roles
  payment_info VARCHAR(255),               -- Can store payment info in a secure way or use a third-party service
  experience INT,                          -- Experience in years (for tutors)
  current_status ENUM('available', 'busy', 'inactive'), -- Tutor’s availability
  workplace VARCHAR(255),                  -- Tutor’s workplace details
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create the main database

CREATE DATABASE IF NOT EXISTS sentence_builder;


USE sentence_builder;

-- Create tables for the data


-- 2. Table for storing individual words and their counts
CREATE TABLE words (
    id INT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL,
    start_count INT UNSIGNED DEFAULT 0,
    end_count INT UNSIGNED DEFAULT 0,
    count INT UNSIGNED DEFAULT 0
);

-- Table for counts of words following other words
-- Add index and a proxy key
CREATE TABLE following (
    combonation_id INT AUTO_INCREMENT PRIMARY KEY,
    word_id1 INT,
    word_id2 INT,
    count INT UNSIGNED DEFAULT 0,
    FOREIGN KEY (word_id1) REFERENCES words(id),
    FOREIGN KEY (word_id2) REFERENCES words(id),
    INDEX follows (word_id1, word_id2)
)

-- Table for file imported and count of words in file
CREATE TABLE files (
    file_name VARCHAR(1000) NOT NULL PRIMARY KEY,
    word_count INT UNSIGNED DEFAULT 0,
)


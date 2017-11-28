CREATE TABLE users
(id VARCHAR(90) PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 username VARCHAR(30) UNIQUE,
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIME,
 is_active BOOLEAN,
 pass VARCHAR(300));

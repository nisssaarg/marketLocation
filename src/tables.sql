CREATE TABLE METADATA (
    metadata_id SERIAL PRIMARY KEY,
    photo_path TEXT,
    location VARCHAR(50) NOT NULL,
    season VARCHAR(50) NOT NULL,
    subject VARCHAR(50) NOT NULL,
    keyword1 VARCHAR(50),
    keyword2 VARCHAR(50),
    keyword3 VARCHAR(50),
    keyword4 VARCHAR(50),
    keyword5 VARCHAR(50)
);

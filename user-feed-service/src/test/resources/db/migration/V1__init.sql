CREATE TABLE activity (
    user_id VARCHAR(255) NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    activity_type VARCHAR(255) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    PRIMARY KEY(user_id, source_id, activity_type)
);
CREATE TABLE user_profile(
    id VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_date TIMESTAMP NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE following(
    follower_user_id VARCHAR(255) NOT NULL REFERENCES user_profile(id),
    following_user_id VARCHAR(255) NOT NULL REFERENCES user_profile(id),
    created_date TIMESTAMP NOT NULL,
    version INT NOT NULL,
    PRIMARY KEY(follower_user_id, following_user_id)
);
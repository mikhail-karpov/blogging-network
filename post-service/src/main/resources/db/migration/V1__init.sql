CREATE TABLE post (
    id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE post_comment (
    id UUID NOT NULL,
    post_fk UUID NOT NULL REFERENCES post(id),
    user_id VARCHAR(255) NOT NULL,
    comment VARCHAR(255) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    PRIMARY KEY(id)
);
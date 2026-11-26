CREATE TABLE authors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    birth_year INTEGER,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    email VARCHAR(150) NOT NULL,
    name VARCHAR(90) NOT NULL,

    CONSTRAINT pk_authors PRIMARY KEY (id),
    CONSTRAINT uk_authors_email UNIQUE (email)
);

CREATE TABLE books (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    isbn VARCHAR(13) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    published_year INTEGER,
    title VARCHAR(200) NOT NULL,
    author_id BIGINT NOT NULL,

    CONSTRAINT pk_books PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn),
    CONSTRAINT fk_books_authors FOREIGN KEY (author_id)
        REFERENCES authors (id)
);
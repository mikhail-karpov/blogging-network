# Blogging Network

Blogging Network is a sample Microservice Architecture with 
following technologies and frameworks:

- Java 8
- Spring Boot
- Spring Cloud
- Spring Data JPA / PostgreSQL
- Spring Security / OAuth2 / Keycloak
- Spring AMQP
- Redis
- JUnit / Testcontainers / Spring Cloud Contract
- Docker
- Maven

The project is under development, but the main functionality has already been 
implemented:

- User has a profile
- User creates a new post (text only)
- User can follow another users
- User can see posts created by users and comment their posts
- User can see posts from users he is following (feed)

## To-Do list

- Finish contract tests with Spring Cloud Contract (in progress)
- Develop a web client
- Implement direct messaging between users

## How to test

Add the contracts and generate the stubs, then run unit and integration tests as usual.

```
mvn install -DskipTests
mvn test
mvn failsafe:integration-test
```

## How to run

Please make sure, your local machine is powerful enough to start 6 Spring Boot 
applications, 4 PostgreSQL instances, Keycloak, Redis and RabbitMQ. 

Before you start, it is recommended to change environment variables and secrets
in `.env` and  `/keycloak/bloggingnetwork-realm.json` files. 
Build the artifacts with Maven, then build, create and start containers 
with Docker. 

```
mvn install -DskipTests
docker compose build
docker compose up
```




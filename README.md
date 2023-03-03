# Blogging Network

Blogging network is a sample microservice architecture with 
following technologies and frameworks:

- Java 11
- Spring Boot
- Spring Cloud
- PostgreSQL
- Keycloak
- RabbitMQ
- Redis
- Testcontainers
- Docker
- Maven

The project is under development, but the main functionality has already been 
implemented:

- User has a profile
- User creates a new post (text only)
- User can follow another users
- User can see posts created by other users and comment their posts
- User can see posts from users he is following (user feed)

## How to run

Please make sure, your local machine is powerful enough to start 6 Spring Boot 
applications, PostgreSQL, Keycloak, Redis and RabbitMQ. 

```
mvn package -DskipTests
docker compose up --build
```

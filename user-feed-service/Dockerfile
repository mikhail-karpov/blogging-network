FROM eclipse-temurin:11
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/user-feed-service.jar .

CMD ["java", "-jar", "/app/user-feed-service.jar"]

EXPOSE 8080
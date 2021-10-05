FROM openjdk:8-alpine
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/user-service.jar .

CMD ["java", "-jar", "/app/user-service.jar"]

EXPOSE 8080
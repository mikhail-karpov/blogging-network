FROM openjdk:8-alpine
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/api-gateway.jar .

CMD ["java", "-jar", "/app/api-gateway.jar"]

EXPOSE 8080
FROM openjdk:8-alpine
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/service-discovery.jar .

CMD ["java", "-jar", "/app/service-discovery.jar"]

EXPOSE 8761
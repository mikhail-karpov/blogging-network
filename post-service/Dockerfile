FROM eclipse-temurin:11
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/post-service.jar .

CMD ["java", "-jar", "/app/post-service.jar"]

EXPOSE 8080
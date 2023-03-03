FROM eclipse-temurin:11
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/config.jar .

CMD ["java", "-jar", "/app/config.jar"]

EXPOSE 8888
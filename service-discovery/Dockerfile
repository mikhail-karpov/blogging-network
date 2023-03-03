FROM eclipse-temurin:11
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

WORKDIR "/app"

COPY ./target/service-discovery.jar .

CMD ["java", "-jar", "/app/service-discovery.jar"]

EXPOSE 8761
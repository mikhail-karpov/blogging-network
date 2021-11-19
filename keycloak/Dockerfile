FROM jboss/keycloak:15.0.2
MAINTAINER Mikhail Karpov <mikhailkarpov@hotmail.com>

COPY bloggingnetwork-realm.json /tmp/bloggingnetwork-realm.json

ENV KEYCLOAK_IMPORT /tmp/bloggingnetwork-realm.json
ENV KEYCLOAK_MIGRATION_STRATEGY OVERWRITE_EXISTING

EXPOSE 8080
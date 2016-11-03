FROM java:openjdk-8u92-jre-alpine
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8443
ENTRYPOINT ["sh", "do-run.sh", "/config"]
VOLUME ["/config"]
COPY felix felix
COPY config /config
COPY do-run.sh do-run.sh
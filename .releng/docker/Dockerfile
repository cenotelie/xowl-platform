FROM java:openjdk-8u92-jre-alpine
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["sh", "run.sh"]
VOLUME ["/config"]
COPY felix felix
COPY config /config
COPY run.sh run.sh
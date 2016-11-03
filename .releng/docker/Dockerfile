FROM java:openjdk-8u92-jre-alpine
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["java", "-Dxowl.conf.dir=/config", "-Dxowl.root=felix", "-jar", "felix/bin/felix.jar", "-b", "felix/bundle"]
VOLUME ["/config"]
COPY felix felix
COPY config /config
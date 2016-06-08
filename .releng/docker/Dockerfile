FROM java:openjdk-8u92-jre-alpine
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["java", "-Dxowl.conf.dir=/config", "-Dxowl.root=felix-framework-5.4.0", "-jar", "felix-framework-5.4.0/bin/felix.jar", "-b", "felix-framework-5.4.0/bundle"]
VOLUME ["/config"]
COPY felix-framework-5.4.0 felix-framework-5.4.0
COPY config /config
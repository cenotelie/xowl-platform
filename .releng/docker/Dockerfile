FROM openjdk:8u131-jre-alpine
LABEL maintainer="Laurent Wouters <lwouters@cenotelie.fr>" vendor="Association Cénotélie" description="xOWL Collaboration Platform"
EXPOSE 8080 8443
COPY xowl-distribution /xowl
COPY org.xowl.platform.kernel.PlatformHttp.ini /xowl/config/org.xowl.platform.kernel.PlatformHttp.ini
ENTRYPOINT ["java", "-Dgosh.args=--noi", "-jar", "/xowl/felix/bin/felix.jar", "-b", "/xowl/felix/bundle"]
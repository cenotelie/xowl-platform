FROM java:openjdk-8u92-jre-alpine
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["java", "-Dxowl.conf.dir=/config", "-jar", "felix-framework-5.4.0/bin/felix.jar", "-b", "felix-framework-5.4.0/bundle"]
VOLUME ["/config"]
RUN wget -q -O org.apache.felix.main.distribution-5.4.0.tar.gz    https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.main.distribution/5.4.0/org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& tar -xz -f org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& rm org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& wget -q -O org.apache.felix.configadmin-1.8.8.jar      https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.configadmin/1.8.8/org.apache.felix.configadmin-1.8.8.jar \
	&& wget -q -O org.apache.felix.eventadmin-1.4.6.jar       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.eventadmin/1.4.6/org.apache.felix.eventadmin-1.4.6.jar \
	&& wget -q -O org.apache.felix.http.api-3.0.0.jar         https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.api/3.0.0/org.apache.felix.http.api-3.0.0.jar \
	&& wget -q -O org.apache.felix.http.base-3.0.4.jar        https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.base/3.0.4/org.apache.felix.http.base-3.0.4.jar \
	&& wget -q -O org.apache.felix.http.servlet-api-1.1.2.jar https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.servlet-api/1.1.2/org.apache.felix.http.servlet-api-1.1.2.jar \
	&& wget -q -O org.apache.felix.http.jetty-3.1.6.jar       https://repo1.maven.org/maven2/org/apache/felix/org.apache.felix.http.jetty/3.1.6/org.apache.felix.http.jetty-3.1.6.jar \
	&& mv *.jar felix-framework-5.4.0/bundle/
COPY *.jar felix-framework-5.4.0/bundle/
COPY config-https.properties felix-framework-5.4.0/conf/
COPY keystore.jks felix-framework-5.4.0/conf/
RUN cat felix-framework-5.4.0/conf/config-https.properties >> felix-framework-5.4.0/conf/config.properties \
    && rm felix-framework-5.4.0/conf/config-https.properties

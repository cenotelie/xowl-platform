FROM java:8
MAINTAINER Laurent Wouters <lwouters@xowl.org>
LABEL description "Docker image for the xOWL federation platform"
EXPOSE 8080
EXPOSE 8443
ENTRYPOINT ["java", "-Dxowl.conf.dir=/config", "-jar", "felix-framework-5.4.0/bin/felix.jar", "-b", "felix-framework-5.4.0/bundle"]
VOLUME ["/config"]
RUN wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& tar -xf org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& rm org.apache.felix.main.distribution-5.4.0.tar.gz \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.configadmin-1.8.8.jar \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.eventadmin-1.4.4.jar \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.http.api-3.0.0.jar \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.http.base-3.0.4.jar \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.http.servlet-api-1.1.2.jar \
	&& wget -nv http://apache.mirrors.ovh.net/ftp.apache.org/dist//felix/org.apache.felix.http.jetty-3.1.4.jar \
	&& mv *.jar felix-framework-5.4.0/bundle/
COPY xowl-*.jar felix-framework-5.4.0/bundle/

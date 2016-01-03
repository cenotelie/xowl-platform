# xOWL Platform - Docker image #

This is a Docker image for the xOWL platform.

## Use ##

```
$ docker pull xowl/xowl-platform:latest
$ docker run -d -p 8080:8080/tcp -p 8443:8443/tcp --name my-xowl-platform -v /path/to/host/config:/config xowl-platform:latest
```

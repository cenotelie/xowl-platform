# xOWL Platform - Docker image #

This is a Docker image for the xOWL platform.

## Use ##

```
$ docker pull xowl/xowl-platform:latest
$ docker run -d -i -p 8080:8080/tcp --name myxowl -v /path/to/config:/config xowl/xowl-platform:latest
```

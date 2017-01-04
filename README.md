# xOWL Platform #

This is the xOWL platform, a set of components for the construction of collaboration platform based on a semantic principles.
The xOWL platform enables the federation of heterogeneous datasets into a semantically coherent common representation so that business analyses can be performed.
Typical applications to system engineering include the federation of Requirements, Functional Architectures, System Architectures, Safety Analyses and more.

## How to use ##

### Downloadable distribution ###

TODO: fill this

### Docker image ###

```
$ docker pull xowl/xowl-platform:latest
$ docker run -d -i -p 8080:8080/tcp --name myxowl -v /path/to/config:/config xowl/xowl-platform:latest
```

Replace the `/path/to/host/config` to a path where to store the configuration for the platform.
With a web-browser, go to [https://localhost:8080/web/](https://localhost:8080/web/).

## License ##

This software is licenced under the Lesser General Public License (LGPL) v3.
Refers to the `LICENSE.txt` file at the root of the repository for the full text, or to [the online version](http://www.gnu.org/licenses/lgpl-3.0.html).


## How to build ##

### Build Java libraries ###

To only build the xOWL libraries as a set of Java libraries, use maven:

```
$ mvn clean install
```

### Build redistributable artifacts ###

To build the redistributable artifacts (server, client, Docker image, etc.):

```
$ ./.releng/build.sh
```

For this, Docker must be locally installed.

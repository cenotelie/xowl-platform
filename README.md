# xOWL Platform #

This repository contains the components for the the xOWL federation platform.

## How to use ##

TODO: add documentation about how to pull and use the docker image

TODO: add documentation about how to extend the platform with domain-specific bundles

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
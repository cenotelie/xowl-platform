# xOWL Collaboration Platform #

[![Build Status](https://dev.azure.com/lwouters/cenotelie/_apis/build/status/cenotelie.xowl-platform?branchName=master)](https://dev.azure.com/lwouters/cenotelie/_build/latest?definitionId=4&branchName=master)

This is the xOWL Collaboration Platform, a set of components for the construction of collaboration platform based on a semantic principles.
The xOWL Collaboration Platform enables the federation of heterogeneous datasets into a semantically coherent common representation so that business analyses can be performed.
Typical applications to system engineering include the federation of Requirements, Functional Architectures, System Architectures, Safety Analyses and more.

## How do I use this software? ##

### xOWL Collaboration Platform ###

The xOWL Collaboration Platform can be used through the [downloadable distribution](https://bitbucket.org/cenotelie/xowl-platform/downloads) or by using the Docker image.

To use the [downloadable distribution](https://bitbucket.org/cenotelie/xowl-platform/downloads) (Java 8 required), simply download it.
Then to administer the platform, use the provided `admin.sh` script:

```
# Launch the platform
$ ./admin.sh start
# Stop the platform
$ ./admin.sh stop
```

With a web-browser, go to [https://localhost:8443/web/](https://localhost:8443/web/).
The default administrator login and password are `admin` and `admin`.

To install the xOWL Collaboration Platform as a linux daemon, simply run (sudo will be asked for):

```
$ ./install-daemon.sh
```

Then, the daemon can be controlled as usual:

```
$ sudo service xowl-platform start
$ sudo service xowl-platform stop
$ sudo service xowl-platform restart
```

The xOWL Collaboration Platform is also available as a Docker image at `nexus.cenotelie.fr/xowl/xowl-platform:latest`.
To run the latest image:

```
$ docker run -d -p 8443:8443 -p 8080:8080 --name my-instance -v /path/to/host/data:/xowl/data nexus.cenotelie.fr/xowl/xowl-platform:latest
```

Replace the `/path/to/host/data` to a path where to store the platform's data on your system.
With a web-browser, go to [https://localhost:8443/web/](https://localhost:8443/web/).
The default administrator login and password are `admin` and `admin`.

### Extends the xOWL Collaboration Platform ###

The xOWL Collaboration Platform can be extended by deploying your own bundles into the platform.
To define your own extension, refer to the paragraph in [xOWL Toolkit](https://bitbucket.org/cenotelie/xowl-toolkit) about how to define a derived platform.


## How to build ##

To build the artifacts in this repository using Maven:

```
$ mvn clean install -Dgpg.skip=true
```

Then, to build the other redistributable artifacts (redistributable package and Docker image):

```
$ ./.releng/build.sh
```

For this, Docker must be locally installed.


## How can I contribute? ##

The simplest way to contribute is to:

* Fork this repository on [Bitbucket](https://bitbucket.org/cenotelie/xowl-platform).
* Fix [some issue](https://bitbucket.org/cenotelie/xowl-platform/issues?status=new&status=open) or implement a new feature.
* Create a pull request on Bitbucket.

Patches can also be submitted by email, or through the [issue management system](https://bitbucket.org/cenotelie/xowl-platform/issues).

The [isse tracker](https://bitbucket.org/cenotelie/xowl-platform/issues) may contain tickets that are accessible to newcomers. Look for tickets with `[beginner]` in the title. These tickets are good ways to become more familiar with the project and the codebase.


## License ##

This software is licenced under the Lesser General Public License (LGPL) v3.
Refers to the `LICENSE.txt` file at the root of the repository for the full text, or to [the online version](http://www.gnu.org/licenses/lgpl-3.0.html).
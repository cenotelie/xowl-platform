This is the distribution for the xOWL Collaboration Platform.


# Licenses

This software is Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation, either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.


# Distribution Content

This distribution contains:

* `LICENSE.txt`, the full text of the GNU LGPL v3 license under which this application is provided.
* `xowl-platform.manifest`, the manifest file containing information about the version of this application

* `admin.sh`, the administration script for starting and stopping the application
* `do-run.sh`, the helper script used by admin.sh to launch the application
* `install-daemon.sh`, the script to install the application as a Linux service
* `uninstall-daemon.sh`, the script to uninstall the Linux service



# Usage

## Manual Administration

To simply launch the application, run

```
$ sh admin.sh start
```

The web application for the administration of the application is available at: https://localhost:8443/web/
The default administrator is:
* login: admin
* password: admin

The application can be stopped or restart from the web application.
Otherwise, it can be managed from the command line:
```
$ sh admin.sh stop
$ sh admin.sh restart
$ sh admin.sh status
```

## Linux Service

To run the server as a Linux service, first register the service with:

```
$ sh install-daemon.sh
```

The service can then be managed with the usual commands:

```
$ service xowl-platform start|stop|status|restart
```

The service can be uninstalled with:

```
$ sh uninstall-daemon.sh
```



# Configuration

TODO: fill this
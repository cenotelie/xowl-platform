# Distribution

A distribution of the xOWL Collaboration Platform is structured as follow:

* Distribution root's directory
    * `config` The directory for the platform's configuration
    * `felix` The felix OSGi platform
        * `felix/bin` The main executable for Felix
        * `felix/bundle` The directory for the platform's bundles
        * `felix/conf` The configuration for Felix
    * `do-run.sh` The script to run a single instance of the platform
    * `admin.sh` In the standalone distribution, the Linux service management script
    * `install-daemon.sh` In the standalone distribution, the Linux service installation
    * `uninstall-daemon.sh` In the standalone distribution, the Linux service un-installation script

Variables bound at runtime:
* `xowl.root` The distribution root's directory
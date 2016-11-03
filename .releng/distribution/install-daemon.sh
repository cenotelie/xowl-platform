#!/bin/sh

SCRIPT="$(readlink -f "$0")"
DISTRIB="$(dirname $SCRIPT)"

echo "Installing xOWL Platform as daemon ..."
echo "xOWL Platform location is $DISTRIB"

rm -f daemon.sh
touch daemon.sh
echo "#!/bin/sh" >> daemon.sh
echo "$DISTRIB/admin.sh \$1" >> daemon.sh

sudo mv daemon.sh /etc/init.d/xowl-platform
sudo chmod +x /etc/init.d/xowl-platform
sudo update-rc.d xowl-platform defaults

echo "OK"

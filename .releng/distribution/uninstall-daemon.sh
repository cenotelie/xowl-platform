#!/bin/sh

echo "Uninstalling xOWL Platform as daemon ..."

sudo rm /etc/init.d/xowl-platform
sudo update-rc.d -f xowl-platform remove

echo "OK"

#!/bin/bash
echo "Installing BitRock InstallBuilder"
wget https://dcprod.duracloud.org/durastore/resources/installbuilder-enterprise-19.12.0-linux-x64-installer.run
chmod +x installbuilder-enterprise-19.12.0-linux-x64-installer.run
# Using sudo to ensure installbuilder is installed to /opt rather than under /home
sudo ./installbuilder-enterprise-19.12.0-linux-x64-installer.run --mode unattended
# Add license file
echo ${BITROCK_INSTALLBUILDER_LICENSE} > /opt/installbuilder-19.12.0/license.xml
echo "Installed BitRock InstallBuilder"

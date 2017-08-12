#!/bin/bash
echo 'Starting before-install.sh'
if [ ! -z "$TRAVIS_TAG" ]; then
    echo `Installing BitRock InstallBuilder`
    wget https://installbuilder.bitrock.com/installbuilder-enterprise-17.7.0-linux-x64-installer.run
    chmod +x installbuilder-enterprise-17.7.0-linux-x64-installer.run
    # Using sudo to ensure installbuilder is installed to /opt rather than under /home
    sudo ./installbuilder-enterprise-17.7.0-linux-x64-installer.run --mode unattended
    # Add license file
    sudo openssl aes-256-cbc -K $encrypted_01c7144b0525_key -iv $encrypted_01c7144b0525_iv -in resources/travis/installbuilder-license.xml.enc -out /opt/installbuilder-17.7.0/license.xml -d
    echo 'Completed before-install.sh'
fi

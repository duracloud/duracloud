#!/bin/bash
echo 'Starting before-install.sh'
if [ ! -z "$TRAVIS_TAG" ] || [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    echo "Installing BitRock InstallBuilder"
    installBuilderVersion = "19.12.0"
    installBuilderInstaller = "installbuilder-enterprise-${installBuilderVersion}-linux-x64-installer.run"
    wget https://dcprod.duracloud.org/durastore/resources/${installBuilderInstaller}
    chmod +x ${installBuilderInstaller} 
    # Using sudo to ensure installbuilder is installed to /opt rather than under /home
    sudo ./${installBuilderInstaller} --mode unattended
    # Add license file
    sudo openssl aes-256-cbc -K $encrypted_01c7144b0525_key -iv $encrypted_01c7144b0525_iv -in resources/travis/installbuilder-license.xml.enc -out /opt/installbuilder-${installBuilderVersion}/license.xml -d
    echo 'Completed before-install.sh'
fi

#!/bin/bash
echo 'Starting before-deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        openssl aes-256-cbc -K $encrypted_01c7144b0525_key -iv $encrypted_01c7144b0525_iv -in resources/travis/codesignkey.asc.enc -out codesignkey.asc -d
        gpg --fast-import codesignkey.asc
        echo 'Completed before-deploy.sh'
    fi
fi

echo TRAVIS_TAG=$TRAVIS_TAG
if [ "$TRAVIS_TAG" != null ]; then
    echo "Building SyncTool installers"
    mvn clean install -DskipTests -Pinstallers -pl synctoolui --settings resources/travis/mvndeploy-settings.xml --batch-mode

    LOCAL_INSTALL=target/install
    mkdir -p  $LOCAL_INSTALL
    echo "Staging install package"
    cp resources/readme.txt ${LOCAL_INSTALL}/
    cp target/durastore.war ${LOCAL_INSTALL}/
    cp target/duradmin.war ${LOCAL_INSTALL}/
    cp target/duradmin.war ${LOCAL_INSTALL}/
    cp target/ROOT.war ${LOCAL_INSTALL}/
    echo "Zipping installation package"
    package="installation-package-${TRAVIS_TAG}.zip"
    zip -r -j target/${package} $LOCAL_INSTALL/

    echo "Staging beanstalk package"
    cd target
    zip -r duracloud-beanstalk-release-${TRAVIS_TAG}.zip duradmin.war durastore.war ROOT.war .ebextensions

    cd ..
    echo "Generating javadocs..."
    mvn javadoc:aggregate -Dadditionalparam="-Xdoclint:none" -Pjava8-disable-strict-javadoc
    cd target/site/apidocs
    zipFile=duracloud-${TRAVIS_TAG}-apidocs.zip
    echo "Zipping javadocs..."
    zip -r duracloud-${TRAVIS_TAG}-apidocs.zip .
fi

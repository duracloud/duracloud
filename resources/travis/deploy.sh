#!/bin/bash
echo 'Starting deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]  || [ ! -z "$TRAVIS_TAG" ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        mvn deploy -DreleaseBuild -DskipTests -DskipDeploy --settings resources/travis/mvndeploy-settings.xml --batch-mode
        echo 'Completed deploy.sh'
  fi
fi

# remove staging files in order to prevent them from being uploaded to s3.
targetDir=$TRAVIS_BUILD_DIR/target
rm -rf nexus-staging ROOT.war .ebextensions duradmin.war durastore.war

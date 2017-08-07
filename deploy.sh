#!/bin/bash
echo 'Starting deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        mvn deploy -DreleaseBuild -DskipTests -DskipDeploy --settings mvndeploy-settings.xml --batch-mode
        echo 'Completed deploy.sh'
  fi
fi

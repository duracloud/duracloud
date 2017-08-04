#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        mvn deploy -DreleaseBuild -DskipTests -DskipDeploy --settings resources/travis/mvndeploy-settings.xml
  fi
fi

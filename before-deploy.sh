#!/bin/bash
echo 'Starting before-deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        openssl aes-256-cbc -K $encrypted_fe4a3fa1215b_key -iv $encrypted_fe4a3fa1215b_iv -in codesigning.asc.enc -out codesigning.asc -d
        gpg --fast-import codesigning.asc
        echo 'Completed before-deploy.sh'
    fi
fi

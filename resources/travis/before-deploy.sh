#!/usr/bin/env bash
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        openssl aes-256-cbc -K $encrypted_fe4a3fa1215b_key -iv $encrypted_fe4a3fa1215b_iv -in resources/travis/codesigning.asc.enc -out resources/travis/codesigning.asc -d
        gpg --fast-import resources/travis/codesigning.asc
    fi
fi

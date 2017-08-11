#!/bin/bash


# function that generates a beanstalk zip based on twp params: s3 bucket and zip file.
generateBeanstalkZip ()
{
   echo "Generating beanstalk zip"
   s3Bucket=$1
   echo "s3Bucket = ${s3Bucket}"
   zipFile=$2
   echo "zipFile=${zipFile}"

    echo "stage beanstalk package"
    mvn process-resources -Ds3.config.bucket=${s3Bucket}  --non-recursive
    cd target
    zip -r ${zipFile} duradmin.war durastore.war ROOT.war .ebextensions
    cd ..
}

echo 'Starting before-deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        openssl aes-256-cbc -K $encrypted_01c7144b0525_key -iv $encrypted_01c7144b0525_iv -in resources/travis/codesignkey.asc.enc -out codesignkey.asc -d
        gpg --fast-import codesignkey.asc
        echo 'Completed before-deploy.sh'
    fi
fi

if [ "$TRAVIS_BRANCH" = 'develop' ]; then
   currentGitCommit=`git rev-parse HEAD`;
   projectVersion=`mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec`
   echo "generating beanstalk zip for $projectVersion ${currentGitCommit}..."
   generateBeanstalkZip "duracloud-dev-config" "duracloud-dev-beanstalk-$projectVersion-${currentGitCommit:0:7}.zip"
fi


echo TRAVIS_TAG=$TRAVIS_TAG

if [ ! -z "$TRAVIS_TAG" ]; then 
    LOCAL_INSTALL=target/install
    mkdir -p  $LOCAL_INSTALL
    echo "stage install package"
    cp resources/readme.txt ${LOCAL_INSTALL}
    cp target/durastore.war ${LOCAL_INSTALL}/
    cp target/duradmin.war ${LOCAL_INSTALL}/
    cp target/duradmin.war ${LOCAL_INSTALL}/
    echo "zip installation package"
    package="installation-package-${TRAVIS_TAG}.zip"
    zip -r -j target/${package} $LOCAL_INSTALL/

    #echo "Generating installers..."
    #mvn clean install -Pinstallers -pl synctoolui

    generateBeanstalkZip "duracloud-production-config" duracloud-production-beanstalk-${TRAVIS_TAG}.zip
    
    echo "Generating  javadocs..."
    # the irodsstorageprovider is excluded due to maven complaining about it. This exclusion will likely be temporary.
    mvn javadoc:aggregate -Dadditionalparam="-Xdoclint:none" -Pjava8-disable-strict-javadoc  -pl \!irodsstorageprovider
    cd target/site/apidocs
    zipFile=duracloud-${TRAVIS_TAG}-apidocs.zip
    echo "Zipping javadocs..."
    zip -r ${zipFile} .
fi


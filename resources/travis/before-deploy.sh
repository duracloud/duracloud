#!/bin/bash
echo 'Starting before-deploy.sh'
if [ "$TRAVIS_BRANCH" = 'master' ] || [ "$TRAVIS_BRANCH" = 'develop' ] || [ ! -z "$TRAVIS_TAG" ]; then
    if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
        echo "Decrypting code signing key"
        openssl aes-256-cbc -K $encrypted_01c7144b0525_key -iv $encrypted_01c7144b0525_iv -in resources/travis/codesignkey.asc.enc -out codesignkey.asc -d
        gpg --fast-import codesignkey.asc
    fi
fi


# function that generates a beanstalk zip
generateBeanstalkZip ()
{ echo "Generating beanstalk zip"
   zipFile=$1
   echo "zipFile=${zipFile}"
   echo "stage beanstalk package"
   mvn process-resources --non-recursive
   cd target
   zip -r ${zipFile} duradmin.war durastore.war ROOT.war .ebextensions
   cd ..
}

targetDir=$TRAVIS_BUILD_DIR/target
currentGitCommit=`git rev-parse HEAD`;
projectVersion=`mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive exec:exec`

if [ "$TRAVIS_BRANCH" = 'develop' ] || [ "$TRAVIS_BRANCH" = 'master' ] || [ ! -z "$TRAVIS_TAG" ]; then
   echo "Generating beanstalk zip for $projectVersion ${currentGitCommit}..."
   beanstalkFile="duracloud-beanstalk-v$projectVersion-${currentGitCommit:0:7}.zip"
   generateBeanstalkZip ${beanstalkFile}

   #make a copy of the beanstalk file using fixed name:
   cp $targetDir/${beanstalkFile} $targetDir/duracloud-beanstalk-latest.zip

   echo "Building SyncTool installers"
   mvn clean install -DskipTests -Pinstallers -pl synctoolui --settings resources/travis/mvndeploy-settings.xml --batch-mode
   # copy artifacts into parent target dir
   cp syncoptimize/target/syncoptimize*-driver.jar $targetDir
   cp retrievaltool/target/retrievaltool*-driver.jar $targetDir
   cp synctoolui/target/duracloudsync*.jar $targetDir
   cp synctoolui/target/duracloudsync*.exe $targetDir
   cp synctoolui/target/duracloudsync*.run $targetDir
   cp synctoolui/target/duracloudsync*.zip $targetDir
fi

if [ ! -z "$TRAVIS_TAG" ]; then
    # build install package only for tagged releases
    LOCAL_INSTALL=$targetDir/install
    mkdir -p  $LOCAL_INSTALL
    echo "Staging install package"
    cp resources/readme.txt ${LOCAL_INSTALL}/
    cp $targetDir/durastore.war ${LOCAL_INSTALL}/
    cp $targetDir/duradmin.war ${LOCAL_INSTALL}/
    cp $targetDir/ROOT.war ${LOCAL_INSTALL}/
    echo "Zipping installation package"
    package="installation-package-${projectVersion}.zip"
    zip -r -j $targetDir/${package} $LOCAL_INSTALL/
    rm -rf ${LOCAL_INSTALL}

    # generate javadocs only for tagged releases
    echo "Generating  javadocs..."
    # the irodsstorageprovider is excluded due to maven complaining about it. This exclusion will likely be temporary.
    # same goes for duradmin and synctoolui due to dependencies on unconventional setup of org.duracloud:jquery* dependencies.
    mvn javadoc:aggregate -Dadditionalparam="-Xdoclint:none" -Pjava8-disable-strict-javadoc  -pl \!irodsstorageprovider,\!duradmin,\!synctoolui --batch-mode
    cd $targetDir/site/apidocs
    zipFile=duracloud-${projectVersion}-apidocs.zip
    echo "Zipping javadocs..."
    zip -r ${zipFile} .
    mv ${zipFile} $targetDir/
    cd $targetDir
    rm -rf install site javadoc-bundle-options

    # generate signed checksum file
    sha512sum * > sha512sum.txt
    echo $GPG_PASSPHRASE | gpg --passphrase-fd 0 --clearsign sha512sum.txt
fi

cd $TRAVIS_BUILD_DIR
echo 'Completed before-deploy.sh'

#! /usr/bin/env bash

if [ -z "$BUILD_HOME" ]; then
  echo 'BUILD_HOME' should be set to the top level of the src tree.
  exit
fi

if [ $# -ne 2 ]; then
  echo "Usage: <initial-version> <new-version>"
  exit
fi

basedir=$BUILD_HOME
envShFile=$basedir/resources/scripts/osgi/env.sh
envBatFile=$basedir/resources/scripts/osgi/env.bat
pomRunFile=$basedir/services/servicesadmin/pom-run.xml

files=($envShFile $envBatFile $pomRunFile)

oldVersion=$1
newVersion=$2

for f in ${files[*]}
do
  perl -pi.bak -e "s/$oldVersion/$newVersion/" $f
done


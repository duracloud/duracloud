#!/bin/sh

echo "=============================="
echo "Starting Coverage Analysis...."
echo "=============================="
echo ""

scriptdir=`dirname "$0"`
. "$scriptdir"/common.sh

echo "=================="
echo "Starting Tomcat..."
echo "=================="
echo ""
if [ -z $CATALINA_HOME ]; then
  echo "ERROR: Need to set CATALINA_HOME"
  exit 1
fi

$CATALINA_HOME/bin/startup.sh


echo ""
echo "================="
echo "Running Clover..."
echo "================="
$MVN clover2:instrument clover2:aggregate clover2:clover -P profile-clover -Dtomcat.port.default=9090 -Ddatabase.home.default=/home/bamboo/duracloud-home/derby/duracloudDB

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Coverage Analysis failed; see above"
  $CATALINA_HOME/bin/shutdown.sh
  exit 1
fi

echo "======================="
echo "Shutting Down Tomcat..."
echo "======================="
echo ""
$CATALINA_HOME/bin/shutdown.sh

echo ""
echo "========================================="
echo "Completed Coverage Analysis Successfully."
echo "========================================="


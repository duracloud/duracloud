#!/bin/sh

echo "========================="
echo "Starting sanity tests...."
echo "========================="
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
echo "==============================================="
echo "Compiling & running unit & integration tests..."
echo "==============================================="
$MVN clean install -Dtomcat.port.default=9090 -Ddatabase.home.default=/home/bamboo/duracloud-home/derby/duracloudDB -Dlog.level.default=DEBUG -Dmaven.test.failure.ignore=true

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Integration test(s) failed; see above"
  $CATALINA_HOME/bin/shutdown.sh
  exit 1
fi

 echo "========================================"
 echo "Building service deployment projects ..."
 echo "========================================"
 chmod +x "$scriptdir"/services.sh
 . "$scriptdir"/services.sh
 
 if [ $? -ne 0 ]; then
   echo ""
   echo "ERROR: Service deployment integration test(s) failed; see above"
   $CATALINA_HOME/bin/shutdown.sh
   exit 1
 fi

echo "======================="
echo "Shutting Down Tomcat..."
echo "======================="
echo ""
$CATALINA_HOME/bin/shutdown.sh


echo ""
echo "===================================="
echo "Completed sanity tests successfully!"
echo "===================================="

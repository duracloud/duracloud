#!/bin/sh

#--------------------------------------------------
# Load environment-specific settings for all tests
#--------------------------------------------------

scriptdir=`dirname "$0"`
. "$scriptdir"/env.sh

#-------------------------------------------
# Echo global settings and export as needed
#-------------------------------------------

echo "[Global Settings]"
echo "JAVA5_HOME    = $JAVA5_HOME"
echo "JAVA6_HOME    = $JAVA6_HOME"
echo "M2_HOME       = $M2_HOME"
echo "BUILD_HOME    = $BUILD_HOME"
echo "CATALINA_HOME = $CATALINA_HOME"
echo "SETTINGS_XML  = $SETTINGS_XML"
echo ""

MVN="$M2_HOME/bin/mvn -s $SETTINGS_XML"

export MVN
export CATALINA_HOME
export SETTINGS_XML

#-------------------------------------------------
# Echo common script options and export as needed
#-------------------------------------------------

echo "[Script Options]"
echo "Arguments     = $*"

# First arg should always specify java5 or java6
if [ $# -lt 1 ]; then
  echo "ERROR: Expected first argument: java5 or java6"
  exit 1
else
  if [ "$1" == "java5" ]; then
    JAVA_HOME=$JAVA5_HOME
  elif [ "$1" == "java6" ]; then
    JAVA_HOME=$JAVA6_HOME
  else
    echo "ERROR: First argument must be java5 or java6"
    exit 1
  fi
fi

export JAVA_HOME

echo "JAVA_HOME     = $JAVA_HOME"

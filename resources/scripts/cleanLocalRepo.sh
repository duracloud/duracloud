#!/bin/sh

scriptdir=`dirname "$0"`

echo "Updating env.sh for DURACLOUD-NIT"
sed_env_cmd="s/DURACLOUD-SAN/DURACLOUD-NIT/"
sed -i.backup -e $sed_env_cmd $scriptdir/env.sh


#-----------------------------------------------------------------------
# Load environment-specific settings, particularly settings.xml location
#-----------------------------------------------------------------------
. "$scriptdir"/env.sh


sed_cmd="s/.*<localRepository>\(.*\)<\/localRepository>.*/\1/p"
localrepo=`sed -n -e $sed_cmd $SETTINGS_XML`
echo "Maven localrepository: '$localrepo'"
if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Unable to find local repository."
  exit 1
fi

echo "Cleaning localrepository..."
rm -rf $localrepo/*

echo "Performing sanity build..."
. "$scriptdir"/on-commit.sh

JAVA=java6

echo "Performing coverage build..."
. "$scriptdir"/coverage.sh ${JAVA}

if [ $? -ne 0 ]; then
  echo ""
  echo "ERROR: Coverage tests failed; see above"
  exit 1
fi

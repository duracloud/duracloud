#!/bin/sh

#-----------------------------------
# Load environment-specific settings
#-----------------------------------

scriptdir=`dirname "$0"`
. "$scriptdir"/../../resources/scripts/osgi/env.sh


cp pom.xml pom.xml.bak

# - DuraCloud -
pax-import-bundle -g org.duracloud -a common -v ${PROJECT_VERSION} -- -DimportTransitive -DwidenScope

# - Other -
pax-import-bundle -g org.slf4j -a com.springsource.slf4j.log4j -v 1.5.0 -- -DimportTransitive -DwidenScope
pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope

mvn clean pax:provision -Dmaven.test.skip=true
chmod +x runner/run.sh

cp pom.xml pom-run.xml
mv pom.xml.bak pom.xml


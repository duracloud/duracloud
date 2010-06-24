#!/bin/sh

#-----------------------------------
# Load environment-specific settings
#-----------------------------------

scriptdir=`dirname "$0"`
. "$scriptdir"/../../resources/scripts/osgi/env.sh


perl -pi.bak -e "s!<packaging>war</packaging>!<packaging>bundle</packaging>!" pom.xml

# - Spring DM -
pax-import-bundle -g org.springframework.osgi -a spring-osgi-core -v 1.2.0
pax-import-bundle -g org.springframework.osgi -a spring-osgi-extender -v 1.2.0
pax-import-bundle -g org.springframework.osgi -a spring-osgi-io -v 1.2.0
pax-import-bundle -g org.springframework.osgi -a spring-osgi-web -v 1.2.0 -- -DimportTransitive
pax-import-bundle -g org.springframework.osgi -a spring-osgi-web-extender -v 1.2.0 -- -DimportTransitive

# - DuraCloud -
pax-import-bundle -g org.duracloud -a common -v ${PROJECT_VERSION} -- -DimportTransitive -DwidenScope
pax-import-bundle -g org.duracloud.services -a servicesutil -v ${PROJECT_VERSION} -- -DimportTransitive -DwidenScope

# - Other -
# pax-import-bundle -g org.slf4j -a com.springsource.slf4j.log4j -v 1.5.0 -- -DimportTransitive -DwidenScope
pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope
pax-import-bundle -g org.apache.commons -a com.springsource.org.apache.commons.fileupload -v 1.2.0 -- -DimportTransitive -DwidenScope

perl -p -e "s!<packaging>bundle</packaging>!<packaging>war</packaging>!" pom.xml > pom-run.xml
mv pom.xml.bak pom.xml

mvn clean -f pom-run.xml pax:provision 
chmod +x runner/run.sh


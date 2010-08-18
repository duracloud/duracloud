cp pom.xml pom.xml.bak

#-----------------------------------
# Load environment-specific settings
#-----------------------------------

scriptdir=`dirname "$0"`
. "$scriptdir"/../../resources/scripts/osgi/env.sh

# - DuraCloud -
pax-import-bundle -g org.duracloud -a storageprovider -v ${PROJECT_VERSION} -- -DimportTransitive -DwidenScope
pax-import-bundle -g org.duracloud -a storeclient -v ${PROJECT_VERSION} -- -DimportTransitive -DwidenScope

mvn clean pax:provision -Dmaven.test.skip=true
chmod +x runner/run.sh

cp pom.xml pom-run.xml
mv pom.xml.bak pom.xml

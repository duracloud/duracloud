cp pom.xml pom-orig.xml

# - Spring DM -
pax-import-bundle -g org.springframework.osgi -a spring-osgi-extender -v 1.2.0 -- -DimportTransitive -DwidenScope
pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope

mvn clean pax:provision -Dmaven.test.skip=true
chmod +x runner/run.sh

cp pom.xml pom-run.xml
mv pom-orig.xml pom.xml

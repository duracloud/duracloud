REM - Load env
call ../../resources/scripts/osgi/env.bat

perl -pi.bak -e "s!<packaging>war</packaging>!<packaging>bundle</packaging>!" pom.xml

REM - Spring DM
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-core -v 1.2.0
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-extender -v 1.2.0
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-io -v 1.2.0
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-web -v 1.2.0 -DimportTransitive
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-web-extender -v 1.2.0 -DimportTransitive

REM - DuraCloud
call pax-import-bundle -g org.duracloud -a common -v %PROJECT_VERSION% -- -DimportTransitive -DwidenScope
call pax-import-bundle -g org.duracloud.services -a servicesutil -v %PROJECT_VERSION% -- -DimportTransitive -DwidenScope

REM - Other
call pax-import-bundle -g org.slf4j -a com.springsource.slf4j.log4j -v 1.5.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g org.apache.commons -a com.springsource.org.apache.commons.fileupload -v 1.2.0 -- -DimportTransitive -DwidenScope

perl -p -e "s!<packaging>bundle</packaging>!<packaging>war</packaging>!" pom.xml > pom-run.xml
move pom.xml.bak pom.xml

call mvn clean -f pom-run.xml pax:provision -Dmaven.test.skip=true

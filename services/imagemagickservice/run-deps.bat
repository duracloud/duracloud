REM - Load env
call ../../resources/scripts/osgi/env.bat

copy pom.xml pom.xml.bak

REM - DuraCloud
call pax-import-bundle -g org.duracloud -a common -v %PROJECT_VERSION% -- -DimportTransitive -DwidenScope
call pax-import-bundle -g org.duracloud.services -a scriptservice -v %PROJECT_VERSION% -- -DimportTransitive -DwidenScope

REM - Other
call pax-import-bundle -g org.slf4j -a com.springsource.slf4j.log4j -v 1.5.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope

copy pom.xml pom-run.xml
move pom.xml.bak pom.xml

call mvn clean -f pom-run.xml pax:provision -Dmaven.test.skip=true

copy pom.xml pom.xml.bak

REM - Spring DM -
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-extender -v 1.2.0 -- -DimportTransitive -DwidenScope

copy pom.xml pom-run.xml
move pom.xml.bak pom.xml

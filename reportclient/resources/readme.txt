---------------------- DuraCloud ReportClient ----------------------

1. Introduction

DuraCloud provides reporting over both storage and service functions
which occur within the DuraCloud framework. The building of these
reports is managed by the DuraReport web application. DuraReport
is installed and running on your DuraCloud instance and can be
accessed via a REST interface. In order to aid Java developers in
communicating with DuraReport, a Java client, called ReportClient
was written.

2. Using ReportClient

To use ReportClient, you will need all of the jars included in the
libs directory to be available on your classpath. You will then be
able to write code using the provided Javadocs to interact with the
ReportClient.

3. Example ReportClient

An example Java class has been provided to assist in set up and
testing, as well as a starting point for writing your client code.
The example client (found in ExampleReportClient.java) includes a
simple main class to print out the list of available reports and
the latest report.

To run the example:
* Extract the report-client zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
  ExampleReportClient.java as needed to connect to your DuraCloud
  instance.
* Make sure Ant is availble on your path and type "ant" to compile
  the example.
* Type "ant run" to run the example.
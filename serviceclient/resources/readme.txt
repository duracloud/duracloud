---------------------- DuraCloud ServiceClient ----------------------

1. Introduction

DuraCloud provides management capabilities to deploy, undeploy,
configure, and gain status of the services within DuraCloud through
an application called DuraService. DuraService is installed and
running on your DuraCloud instance and can be accessed via a REST
interface. In order to aid Java developers in communicating with
DuraService, a Java client, called ServiceClient was written.

2. Using ServiceClient

To use ServiceClient, you will need all of the jars included in the
libs directory to be available on your classpath. You will then be
able to write code using the provided Javadocs to interact with the
ServiceClient.

3. Example ServiceClient

An example Java class has been provided to assist in set up and
testing, as well as a starting point for writing your client code.
The example client (found in ExampleServceClient.java) includes a
simple main class to print out the list of services available in
your DuraCloud account, then run the bit-integrity-service over
one of the spaces in your account.

To run the example:
* Extract the service-client zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
  ExampleServiceClient.java as needed to connect to your DuraCloud
  instance.
* Make sure Ant is availble on your path and type "ant" to compile
  the example.
* Type "ant run" to run the example, which will prompt for the name
  of a space over which to run the bit-integrity-service.
  If you do not know which spaces are available, enter no value and
  the names of all valid spaces will be shown.
* If you are running a local instance of DuraCloud, you will need
  to ensure that your OSGi services container is running and that
  DuraService has been properly initialized.
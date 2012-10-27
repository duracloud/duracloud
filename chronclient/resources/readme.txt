--------------------------- ChronopolisClient ----------------------------------

1. Introduction

The Chronopolis REST server manages requests to store and restore DuraCloud
spaces and content to/from Chronopolis. The content that flows between the two
systems, DuraCloud and Chronopolis, uses the SDSC storage provider as the
interim staging area. In order to aid Java developers in communicating with the
Chronopolis REST server, a Java client called ChronopolisClient was written.

2. Using ChronopolisClient

To use ChronopolisClient, you will need all of the jars included in the libs
directory to be available on your classpath. You will then be able to write code
using the provided Javadocs to interact with the ChronopolisClient.

3. Example ChronopolisClient

An example Java class has been provided to assist in set up and testing, as well
as a starting point for writing your client code. The example client (found in
ExampleChronopolisClient.java) includes a simple main class to make a space
backup request, monitor the status of that request, and restore both a single
item as well as the full space.

To run the example:
* Extract the chronopolis-client zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
  ExampleChronopolisClient.java as needed to connect to your Chronopolis REST
  server.
* Make sure Ant is availble on your path and type "ant" to compile the example.
* Type "ant run" to run the example.
---------------------- DuraCloud StoreClient ----------------------

1. Introduction

DuraCloud provides access to files stored in cloud storage systems
through an application called DuraStore. DuraStore is installed and
running on your DuraCloud instance and can be accessed via a REST
interface. In order to aid Java developers in communicating with
DuraStore, a Java client, called StoreClient was written.

2. Using StoreClient

To use StoreClient, you will need all of the jars included in the
libs directory to be available on your classpath. You will then be
able to write code using the provided Javadocs to interact with the
StoreClient.

3. Example Client

An example Java class has been provided to assist in set up and
testing, as well as a starting point for writing your client code.
The example client (found in ExampleClient.java) includes a simple 
main class to print out the list of spaces and space metadata 
available in your DuraStore account. To run the example:
* Extract the store-client zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
ExampleClient.java as needed to connect to your DuraCloud instance.
* Make sure Ant is availble on your path and type "ant" to compile
the example.
* Type "ant run" to run the example, which will print space names
and metadata to the console.
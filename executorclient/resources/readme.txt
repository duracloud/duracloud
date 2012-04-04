--------------------- DuraCloud ExecutorClient ---------------------

1. Introduction

The DuraCloud executor manages the running of actions over storage
and services in DuraCloud. The Executor is part of the DuraBoss
application, which is installed and running on your DuraCloud
instance and can be accessed via a REST interface. In order to aid
Java developers in communicating with DuraBoss executor features,
a Java client, called ExecutorClient was written.

2. Using ExecutorClient

To use ExecutorClient, you will need all of the jars included in the
libs directory to be available on your classpath. You will then be
able to write code using the provided Javadocs to interact with the
ExecutorClient.

3. Example ExecutorClient

An example Java class has been provided to assist in set up and
testing, as well as a starting point for writing your client code.
The example client (found in ExampleExecutorClient.java) includes a
simple main class to print out the list of available actions and
the current Executor status.

To run the example:
* Extract the executorclient zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
  ExampleExecutorClient.java as needed to connect to your DuraCloud
  instance.
* Make sure Ant is availble on your path and type "ant" to compile
  the example.
* Type "ant run" to run the example.
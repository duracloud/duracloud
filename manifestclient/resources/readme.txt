-------------------- DuraCloud ManifestClient --------------------

1. Introduction

The DuraCloud manifest project allows for the retrieval of
manifest files for DuraCloud spaces. These manifest files list
the content items and their associated checksums for all files
in the given space. The manifest project is part of the DuraBoss
application, which is installed and running on your DuraCloud
instance and can be accessed via a REST interface. In order to aid
Java developers in communicating with DuraBoss manifest features,
a Java client, called ManifestClient was written.

2. Using ManifestClient

To use ManifestClient, you will need all of the jars included in the
libs directory to be available on your classpath. You will then be
able to write code using the provided Javadocs to interact with the
ManifestClient.

3. Example ManifestClient

An example Java class has been provided to assist in set up and
testing, as well as a starting point for writing your client code.
The example client (found in ExampleManifestClient.java) includes a
simple main class to print out the manifest of a space. You will need
to set the name of the space in the class to point to a space that
exists in your DuraCloud account.

To run the example:
* Extract the manifestclient zip file
* Update the HOST, PORT, USERNAME, and PASSWORD constant values in
  ExampleManifestClient.java as needed to connect to your DuraCloud
  instance.
* Update the STORE_ID and SPACE_ID values to indicate the store and
  space you would like to run the example over. Note that leaving
  STORE_ID as null will make use of the primary storage provider.
* Make sure Ant is availble on your path and type "ant" to compile
  the example.
* Type "ant run" to run the example.
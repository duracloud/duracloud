---------------------- DuraCloud StoreClient ----------------------

1. Introduction

DuraCloud provides access to files stored in cloud storage systems
through an application called DuraStore. DuraStore is installed and
running on your DuraCloud instance and can be accessed via a REST
interface. In order to aid Java developers in communicating with
DuraStore, a Java client, called StoreClient was written.

2. Using StoreClient

To use StoreClient, you will need all of the jars the client depends
on. This is best accomplished by using Maven:

<dependency>
  <groupId>org.duracloud</groupId>
  <artifactId>storeclient</artifactId>
  <version>{duracloud-version}</version>
</dependency>

Alternatively, you can include the jar files in the libs directory
on your classpath. You will then be able to write code using the
provided Javadocs to interact with the StoreClient.

3. Example Client

An example Maven project has been provided to assist in set up and
testing, as well as a starting point for writing your client code.

This client is available for download from Github here:
https://github.com/duracloud/rest-client-example
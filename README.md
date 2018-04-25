<p align="center">
  <img src="https://wiki.duraspace.org/download/attachments/31655033/duracloud_logo_4in.png?version=1&modificationDate=1329183208802&api=v2" alt="DuraCloud Logo"  width="100%" />
</p>

[DuraCloud Documentation](https://wiki.duraspace.org/display/DURACLOUD) | [DuraCloud Downloads](https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Downloads) | [DuraCloud Releases](https://github.com/duracloud/duracloud/releases) | [DuraCloud REST API](https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+REST+API)

[![DuraCloud Gitter](https://badges.gitter.im/duracloud/duracloud.svg)](https://gitter.im/duracloud/duracloud?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

:boom: [Are you here for the Global Sprint? Start here!](MOZSPRINT.md) :boom: 

## Table of Contents

* [What is DuraCloud?](#what-is-duracloud)
* [Why DuraCloud?](#why-duracloud)
* [History](#history)
* [Roadmap](#roadmap)
* [Documentation](#documentation)
* [Overview](#overview)
* [Code of Conduct](#code-of-conduct)
* [Contribute](#contribute)
* [License](#license)

## What is DuraCloud?

DuraCloud is a suite of open source software applications used together to manage files stored in a variety of third-party storage systems. DuraCloud connects to storage in commercial cloud systems, academic storage systems, and national preservation networks, providing consistent interfaces through which content can be managed. DuraCloud also handles the duplication of content across providers and performs bit-level integrity checking to verify that stored content remains unchanged.

We invite anyone interested in digital preservation, especially preservation for the common good, to help us maintain, improve, and deploy DuraCloud to prevent the loss of culturally relevant digital content. 

## Why DuraCloud?

In the digital era, ensuring that critically important documents remain safe and available is a continual challenge. Physical computing hardware that is used to create and store documents can fail or become obsolete very quickly, providing a need for tools to ensure that these documents remain available.

There are many options for file storage and backup, with a growing trend toward the use of service providers offering off-site storage and backup solutions. These solutions are enticing, but several concerns often remain:

1. How do I ensure that files remain intact over time after I have transferred them to a storage service?
2. How do I ensure that each of the storage services that I am using receives a copy of my local files?
3. How do I retrieve my content once it is stored?
4. How can I know in advance the amount I will be required to pay for storage?
5. How can I guard against storage services changing their pricing, terms of service, or even going out of business?

DuraCloud was created to take on these concerns, allowing you to use world-class storage systems with confidence. How does DuraCloud solve these problems?

1. DuraCloud provides on-going bit-level verification checks for stored files. On a schedule, DuraCloud retrieves every file from the storage provider and uses a [cryptographic checksum](http://searchsecurity.techtarget.com/definition/cryptographic-checksum) to verify that the file has not changed.
2. DuraCloud provides tools, installed on your local system, that handle copying all files into storage. These tools can be configured to transfer files one time, or to continually watch for changes and transfer files as they are created or updated. DuraCloud also provides duplication of content across providers, so there is no need to transfer files to more than one place.
3. In most cases, DuraCloud content is available for immediate download. Files can be selected through the user interface and tools are provided which will handle bulk downloads. When the storage provider is a [dark archive](https://www2.archivists.org/glossary/terms/d/dark-archives), only one additional step is required to make content ready to access. Content listings are always visible for all providers, and retrieving content is the same process regardless of where the content is stored.
4. The amount you pay for DuraCloud is negotiated up-front with the DuraCloud host; there are no hidden fees, and pricing is intentionally designed to remove as many cost variables as possible. If you would prefer to host DuraCloud yourself, you may do so at any time; all of the code is open source and all documentation is freely available.
5. DuraCloud integrates with multiple storage providers, providing you the option to change the providers in which your content is stored at any time. Your DuraCloud host will also be happy to help with this transition. In all cases, the way you interact with DuraCloud will not change; even as you transition from one storage provider to another, the way you view and access the content will always remain consistent.

## History

DuraCloud was created by [DuraSpace](http://duraspace.org/), a non-profit organization, with initial funding provided by the Gordon and Betty Moore Foundation, the Andrew W. Mellon Foundation, and the [Library of Congress](https://www.loc.gov/item/prn-09-140/library-of-congress-and-duracloud-launch-pilot-program/2009-07-14/). DuraCloud was built to provide a hosted preservation storage solution as a service for those charged with managing digital content, but who do not have the tools to ensure that content is preserved.

## Roadmap

Want to know what we're working on? Take a look at the [DuraCloud Roadmap](https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Roadmap).

## Documentation

DuraCloud documentation is found on the [DuraCloud Wiki](https://wiki.duraspace.org/display/DURACLOUD/DuraCloud). Some of the most popular pages:
* [DuraCloud Guide](https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Guide) - Answers to Frequently Asked Questions
* [DuraCloud Architecture](https://wiki.duraspace.org/display/DURACLOUD/DuraCloud+Architecture) - How DuraCloud works
* [DuraCloud REST API](https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+REST+API) - How calls are made to DuraCloud
* [DuraCloud SyncTool](https://wiki.duraspace.org/display/DURACLOUDDOC/DuraCloud+Sync+Tool) - The best way to get content to DuraCloud in bulk

## Overview

DuraCloud is an open source software product and a hosted service. This summary articulates our current and aspirational parameters of product and community development, community engagement, project execution, and unique value proposition.

<p align="center">
  <img src="https://wiki.duraspace.org/download/attachments/96993626/open-canvas-duracloud.jpg" alt="DuraCloud Open Canvas"  width="100%" />
</p>

## Code of Conduct

The DuraCloud project is goverened by the [DuraSpace Code of Conduct](http://www.duraspace.org/community/codeofconduct). By participating, you are agreeing to abide by this code. 

## Contribute

We welcome contributions to DuraCloud! You do not need to be able to write code to pitch in. See the [CONTRIBUTING](CONTRIBUTING.md) page or click on one of these links for more details:

* [I'm a Java developer](CONTRIBUTING.md#im-a-java-developer)
* [I'm a DuraCloud user](CONTRIBUTING.md#im-a-duracloud-user)
* [I'd like to get involved](CONTRIBUTING.md#id-like-to-get-involved)

## License

DuraCloud is licensed under the terms of the [Apache 2](https://www.apache.org/licenses/LICENSE-2.0) license

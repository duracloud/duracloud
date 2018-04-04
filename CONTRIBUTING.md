**First of all, thanks for your interest in contributing to the DuraCloud project!** :tada:

There are many ways to contribute, first look for a section which describes you, then dive in.

* [I'm a Java developer](#im-a-java-developer)
* [I'm a DuraCloud user](#im-a-duracloud-user)
* [I'd like to get involved](#id-like-to-get-involved)

Do you have an idea which doesn't fit into one of these categories? Are you still not sure how to get started? Then join the [mailing list](https://groups.google.com/group/duracloud-users) and ask a question or make a suggestion, we'd be happy to talk through your ideas and find ways for you to contribute.

## I'm a Java developer

Awesome! You can use your skills to help us develop new features for DuraCloud, or squash some bugs. 

Before you can start making code changes, you will need to get DuraCloud deployed. Take a look first at the [deployment docs](https://wiki.duraspace.org/display/DURACLOUDDOC/Building+DuraCloud+from+Source), then ask on the [mailing list](https://groups.google.com/group/duracloud-users) if you run into trouble or have questions. If you would prefer to deploy in AWS, the [production deployment documentation](https://github.com/duracloud/deployment-docs) will provide guidance.

We welcome suggestions or recommendations that will make this documentation better or make the deployment simpler. We know there is work to be done there, so your assistance is appreciated.

Once you have DuraCloud running, you can contribute in two ways:

### Test

We can always use more eyes looking for issues, especially when the hands connected to those eyes can help to fix those issues! Use DuraCloud, add content, add properties, update, delete, then look to see if anything isn't working quite right. 

See something amiss? Check [JIRA](https://jira.duraspace.org/projects/DURACLOUD) to see if it is a known issue. If it is, feel free to leave a comment saying you've been able to reproduce the issue in your environment. If there is not already a ticket in JIRA, create one. 

When writing a ticket, make sure to include:

* A clear description of the issue
* Which components are being tested (the DuraCloud UI, the SyncTool, the REST API)
* How to reproduce the issue
* Details about your testing environment: OS, Java version, browser type and version, etc

### Develop

Ready to start diving into code?

1. Look for a [JIRA ticket](https://jira.duraspace.org/projects/DURACLOUD) which interests you, or create a new one.
2. [Fork the code repository](https://help.github.com/articles/fork-a-repo/) in Github. There are several repos for DuraCloud, the primary one is: https://github.com/duracloud/duracloud
3. Make your changes, do fantastic things, just make sure:
    * Your changes work
    * Unit tests are included which exercise your changes
    * The build passes
    * You are following the [code style guidelines](https://github.com/duraspace/codestyle)
4. Commit to a branch on your fork, and [submit a pull request](https://help.github.com/articles/about-pull-requests/). The pull request should be against the **develop** branch of the DuraCloud repository (not master).


## I'm a DuraCloud user

Already have an account? There are many ways to help:

* Is there a feature in DuraCloud or an enhancement to an existing feature you'd really like to see developed? Create a [JIRA ticket](https://jira.duraspace.org/projects/DURACLOUD), then talk with us about it on the [mailing list](https://groups.google.com/group/duracloud-users). We can't make guarantees about when or if features will be developed, but if we don't know about it, you can be sure it won't happen.
  * If you have a hosted DuraCloud account and run into a problem, start by shooting a message to DuraCloud support at support@duracloud.org. They will help you track down the issue and will log a bug report in JIRA for you if necessary.
* Did you notice something wrong or missing in the [documentation](https://wiki.duraspace.org/display/DURACLOUD)? Add a comment to the page so we can get it updated. If you would like to help more with documentation, just let us know!
* Speaking of the [mailing list](https://groups.google.com/group/duracloud-users), many users of DuraCloud have questions about local processes for organizing, preparing, and transferring content to DuraCloud. Help to answer questions, or start a conversation with a list of tips from what you have learned.

## I'd like to get involved

Fantastic! We can use your help!

If you are looking for a place to start, take a look at the [issues listed in Github](https://github.com/duracloud/duracloud/issues). These are intentionally targetted toward non-development activities, and are all good places to get your feet wet.

Do you have an idea for an interest or working group? Join the [mailing list](https://groups.google.com/group/duracloud-users) and let us know your ideas. You are likely to not be the only one interested in digging deeper.

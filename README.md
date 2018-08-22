# OLAT | Online Learning and Training

OLAT is a mature [Learning Management System](https://en.wikipedia.org/wiki/Learning_management_system) (LMS) with a nearly twenty-year track record. It has been developed since 1999 at [University of Zurich](https://www.uzh.ch) (UZH). OLAT is hosted by the central service unit Information Technology and is available to all faculties and institutes at UZH as well as to other [Swiss universities](https://www.swissuniversities.ch/en) and higher education institutions.

To learn more, visit [olat.org](https://olat.org)

#### OLAT Feature Modules
* ``lmsuzh-extension-campuskurs`` Synchronized course management with [SAP Campus Management](https://www.id.uzh.ch/de/dl/sapplus/alle.html)
* ``lmsuzh-extension-opencast`` Lectures online with [Opencast](https://opencast.org)
* ``lmsuzh-extension-adobeconnect`` Live teamwork with [Adobe Connect](https://switch.ch/interact)
* ``lmsuzh-extension-epis`` Exam preparation, implementation and support powered by [EPIS services](https://www.id.uzh.ch/de/dl/elearning/epis.html)
* ``lmsuzh-extension-teachingsurvey`` Survey module for the [UZH Teaching Award](https://www.uzh.ch/de/studies/teaching/lehrpreis.html)
* ``lmsuzh-extension-mindmaps`` [Take notes, visualize and outline ideas](http://olat.systems/mindmaps) (Proof-of-concept)
* ``lmsuzh-extension-flashcards`` [Recall the solution written on a flash card](http://olat.systems/flashcards) (Proof-of-concept)
* ``lmsuzh-extension-myscript`` [Convert handwritten formulas, texts and shapes](http://olat.systems/handwriting) (Proof-of-concept)

#### OLAT Infrastructure Modules
* ``lmsuzh-extension-continuousintegration`` CI/CD pipeline based on [Maven](https://maven.apache.org), [Jenkins](https://jenkins.io) and [Rancher](https://rancher.com)
* ``lmsuzh-extension-codemanagement`` Source code and configuration management based on [Mercurial](https://www.mercurial-scm.org) and [Git](https://www.git-scm.com)
* ``lmsuzh-extension-docker`` Container technology integration based on [Docker](https://www.docker.com)
* ``lmsuzh-extension-database`` Persistence containers based on [PostgreSQL](https://www.postgresql.org) (deprecated since 7/2018: [MySQL](https://www.mysql.com))
* ``lmsuzh-extension-embedded`` HTTP server and Servlet container based on [Jetty](https://www.eclipse.org/jetty)
* ``lmsuzh-extension-restclient`` Connector for web services based on [REST](https://en.wikipedia.org/wiki/Representational_state_transfer)
* ``lmsuzh-extension-loadtest`` Performance testing based on [JMeter](https://jmeter.apache.org)
* ``lmsuzh-extension-functionaltest`` Automated testing based on [Selenium](https://www.seleniumhq.org)
* ``lmsuzh-extension-scenariospecification`` Behavior-driven development approach based on [JGiven](http://jgiven.org)

#### OLAT Core Modules
* ``lmsuzh-extension-core`` Extensions core
* ``lmsuzh-extension-tool`` Module utilities
* ``lmsuzh-extension-config`` Settings and themes
* ``lmsuzh-extension-war`` Web application archive
* ``lmsuzh-extension-buildtools`` Building helpers
* ``lmsuzh-extension-olatreplacement`` Bridging helpers
* ``openolat-lms`` OpenOLAT project

#### OLAT Java Development Environment

Grab the OLAT Source code
```
$ git clone https://github.com/olatsystems/olat-extension-modules lmsuzh-extension-DEVELOPMENT
$ git clone https://github.com/olatsystems/olat-core-library openolat-lms-DEVELOPMENT
```

Build the OLAT Environment (skipping the CI/CD infrastructure)
```
$ cd openolat-lms-DEVELOPMENT
$ mvn clean install -Ptomcat,compressjs -DskipTests=true -DskipSeleniumTests=true

$ cd lmsuzh-extension-DEVELOPMENT
$ mvn clean install -DskipTests=true -Ddocker.skip=true

$ cd lmsuzh-extension-DEVELOPMENT/lmsuzh-extension-docker
$ mvn -Plocal-docker-registry docker:start

$ cd lmsuzh-extension-DEVELOPMENT/lmsuzh-extension-database
$ mvn package -Dmaven.main.skip docker:start
```

Run the OLAT Java app
```
$ cd lmsuzh-extension-DEVELOPMENT
$ java -javaagent:${HOME}/.m2/repository/org/aspectj/aspectjweaver/1.9.0/aspectjweaver-1.9.0.jar -ea \
       -jar lmsuzh-extension-embedded/target/lmsuzh-extension-embedded-1.0-SNAPSHOT.jar
```

#### COMING SOON: Run OLAT Docker apps
```
$ docker run olatsystems/olat-app
$ docker run olatsystems/opencast-app
```

To learn more, visit [olat.technology/getting/started](http://olat.technology/getting/started) and [olat.technology/digging/deeper](http://olat.technology/digging/deeper)

## License

The OLAT open source code is licensed under the terms of the [Apache license](http://www.apache.org/licenses/LICENSE-2.0).

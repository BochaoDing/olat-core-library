# OLAT Core Library based on OpenOLAT

**OLAT (Online Learning and Training)** is a mature Learning Management System (LMS) with a nearly twenty-year track record. It has been developed since 1999 at [University of Zurich (UZH)](https://www.uzh.ch).

OLAT is hosted by the Department of Information Technology and is available to all faculties and institutes at University of Zurich as well as to other Swiss universities and higher education institutions. OLAT supports strongly the Open Educational Resources (OER) approach in cooperation with [SWITCH edu-ID](https://www.switch.ch/edu-id/).

To learn more about OLAT, visit [olat.org](https://olat.org)

## Changelog

**~~2018/06/06~~**
**2018/07/04**
* Fixed an issue with session timeout for some legacy courses
* Fixed an issue with empty search strings
* Fixed an issue with language setting on sign up
* Fixed an issue with question pool usage info
* Fixed an issue with cleanup handling

**2018/05/09**
* Added SWITCHdrive & SWITCHportfolio as personal tools
* Added option to personal settings to show/stash hidden files
* Improved Campuskurs creation upfront of a semester
* Fixed issues with mail forwarding to external addresses
* Fixed an issue with SWITCHcast for more than 100 videos per series
* Corrected spelling and punctuation

**2018/04/11**
* Improved SWITCHcast module to support multiple tenants
* Improved SWITCHinteract module for administrators
* Fixed issues with Microsoft Internet Explorer

**2018/02/28**
* Improved user interface and user profile infos for Campuskurs
* Improved paging in authoring mode
* Improved usability in course elements Task and Participant folder

**2018/02/07**
* Added SWITCHinteract as new course element for Web Meetings based on Adobe Connect (see [OLAT-User-Manual](https://help.olat.uzh.ch/display/OO114EN/Communication+and+collaboration#Communicationandcollaboration-Createmeetings))
* Fixed issues in Campuskurs module
* Improved context help using [help.olat.uzh.ch](https://help.olat.uzh.ch)
* Improved usability in member list course element

## Overview

### Feature modules

* ``lmsuzh-extension-campuskurs`` Synchronized course management
* ``lmsuzh-extension-opencast`` Lectures online
* ``lmsuzh-extension-adobeconnect`` Live teamwork
* ``lmsuzh-extension-epis`` Digital assistance for exams
* ``lmsuzh-extension-registration`` Group registration processes for large installations
* ``lmsuzh-extension-teachingaward`` Survey on the awarding of the teaching prize
* ``lmsuzh-extension-assessment`` SELMA: [Self and mass assessments in a university context](http://olat.systems/whitepaper)
* ``lmsuzh-extension-mindmaps`` Proof-of-concept: [Take notes, visualize and outline ideas](http://olat.systems/mindmaps)
* ``lmsuzh-extension-flashcards`` Proof-of-concept: [Recall the solution written on a flash card](http://olat.systems/flashcards)
* ``lmsuzh-extension-myscript`` Proof-of-concept: [Convert handwritten formulas, texts and shapes](http://olat.systems/handwriting)

### Infrastructure modules

* ``lmsuzh-extension-continuousintegration`` Maven/Jenkins/Rancher park
* ``lmsuzh-extension-docker`` Container technology integration
* ``lmsuzh-extension-database`` Persistence containers
* ``lmsuzh-extension-embedded`` Jetty runtime
* ``lmsuzh-extension-restclient`` REST connector
* ``lmsuzh-extension-functionaltest`` Automated testing
* ``lmsuzh-extension-loadtest`` Performance testing

### Core modules

* ``lmsuzh-extension-core`` Extensions core
* ``lmsuzh-extension-tool`` Module utilities
* ``lmsuzh-extension-config`` Settings and themes
* ``lmsuzh-extension-war`` Web application archive
* ``lmsuzh-extension-buildtools`` Building helpers
* ``lmsuzh-extension-olatreplacement`` Bridging helpers
* ``openolat-lms`` OpenOLAT project

## License

The OLAT open source code is licensed under the terms of the [Apache license](http://www.apache.org/licenses/LICENSE-2.0).

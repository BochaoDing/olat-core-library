#!/bin/sh

JVM_ARGS="-Xms512m -Xmx512m" jmeter -p OLAT_LMS.properties -t OLAT_LMS.jmx

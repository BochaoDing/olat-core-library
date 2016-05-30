#!/bin/sh

JVM_ARGS="-Xms512m -Xmx512m" jmeter -p OpenOLAT104_LMS.properties -t OpenOLAT104_LMS.jmx

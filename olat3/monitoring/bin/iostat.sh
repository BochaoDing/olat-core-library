#!/bin/bash

rv=-1
wv=-1
val=`iostat -x /dev/dm-13 5 2 | tail -n 2 | grep dm-13 | awk '{ print $6" "$7 }'`
if [ -n "${val}" ] ; then
  rv=`echo ${val} | cut -d ' ' -f 1 | cut -d '.' -f 1`
  wv=`echo ${val} | cut -d ' ' -f 2 | cut -d '.' -f 1`
  # rv=`cat ${userdata.dir}/tmp/iostat | awk '{print $3}'`
  # wv=`cat ${userdata.dir}/tmp/iostat | awk '{print $4}'`
fi
[ -z "${rv}" ] && rv=0
[ -z "${wv}" ] && wv=0
echo "$rv"
echo "$wv"
date
echo "${server.domainname}"
#!/bin/sh
# *****************************************************************
#
# HCL Confidential
#
# OCO Source Materials
#
# Copyright HCL Technologies Limited 2014, 2021
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# *****************************************************************

cd `dirname $0`

LOCK_FILE=./sync_all_dns.lck

# test lock
if [ -e ${LOCK_FILE} ]; then
	echo "Synchronization Lock file already exist. Please turn off other running sync process before performing this one."
	exit
else
	echo "create synchronization lock"
	touch ${LOCK_FILE}
fi

#clear rc file
if [[ -e ./_tdi.rc ]]; then
  rm -f ./_tdi.rc
fi

UNAME_OS=`uname`
if [ "$UNAME_OS" = "OS400" ] ; then
	touch -C 819 ./_tdi.rc
fi

#get TDI variables
. ./tdienv.sh

./netstore ping >/dev/null 2>/dev/null
if [[ $? -ne 0 ]]; then
	./netstore start
fi

#set initial failure return code in case called task does not complete
echo "1" >./_tdi.rc
${TDIPATH}/ibmdisrv -s . -c profiles_tdi.xml -r sync_all_dns
rc=`cat ./_tdi.rc`
if [[ ! "${rc}" == "0" ]]; then
	echo "Synchronize of Database Repository failed"
	echo ""
fi

#clear rc file
if [[ -e ./_tdi.rc ]]; then
  rm -f ./_tdi.rc
fi

UNAME_OS=`uname`
if [ "$UNAME_OS" = "OS400" ] ; then
	touch -C 819 ./_tdi.rc
fi

./clearLock.sh

exit ${rc}

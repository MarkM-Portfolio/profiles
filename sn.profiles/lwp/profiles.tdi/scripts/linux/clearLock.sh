#!/bin/sh
# *****************************************************************
#
# HCL Confidential
#
# OCO Source Materials
#
# Copyright HCL Technologies Limited 2010, 2021
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# *****************************************************************

LOCK_FILE=./sync_all_dns.lck

if [ -e ${LOCK_FILE} ]; then
	echo "release sync lock"
	rm -f ${LOCK_FILE}
else
	echo "cannot find sync lock"
fi

#!/bin/sh
# *****************************************************************
#
# HCL Confidential
#
# OCO Source Materials
#
# Copyright HCL Technologies Limited 2009, 2021
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# *****************************************************************

cd `dirname $0`

#get TDI variables
. ./tdienv.sh

./netstore ping >/dev/null 2>/dev/null
if [[ $? -ne 0 ]]; then
	./netstore start
fi
${TDIPATH}/ibmdisrv -s . -c profiles_tdi.xml -r process_tds_changes

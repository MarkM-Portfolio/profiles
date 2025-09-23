#!/bin/sh
# *****************************************************************
#
# HCL Confidential
#
# OCO Source Materials
#
# Copyright HCL Technologies Limited 2015, 2021
#
# The source code for this program is not published or otherwise
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.
#
# *****************************************************************

cd `dirname $0`

#clear rc file
./clearRC.sh

#get TDI variables
. ./tdienv.sh

./netstore ping >/dev/null 2>/dev/null
if [[ $? -ne 0 ]]; then
	./netstore start
fi

#set initial success until success/failure coded (if ever)
echo "0" >./_tdi.rc
${TDIPATH}/ibmdisrv -s . -c profiles_tdi.xml -r create_timestamp_table_DB2_TEST
rc=`cat ./_tdi.rc`
if [[ ! "${rc}" == "0" ]]; then
	echo "create_timestamp_table failed"
fi

./clearRC.sh
exit ${rc}

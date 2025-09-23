#!/bin/sh
# ***************************************************************** 
#                                                                   
# IBM Confidential                                                  
#                                                                   
# OCO Source Materials                                              
#                                                                   
# Copyright IBM Corp. 2010, 2015                                    
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

#set initial failure return code in case called task does not complete
echo "1" >./_tdi.rc
${TDIPATH}/ibmdisrv -s . -c revoke_users.xml -r $1

rc=`cat ./_tdi.rc`
if [[ ! "${rc}" == "0" ]]; then
	echo "Revoke users failed"
	echo ""
fi
./clearRC.sh
exit ${rc}

#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# 1.25, 7/2/09
#
# Licensed Materials - Property of IBM
#
# Restricted Materials of IBM
#
# (C) COPYRIGHT International Business Machines Corp. 2005, 2010
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

#################################################################
# cryptoutils command
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
}

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER=which

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER=whence
fi

TEMP_BIN_DIR=`$CMDFINDER $0`
TEMP_BIN_DIR=`dirname $TEMP_BIN_DIR`
TEMP_BIN_DIR=`dirname "$TEMP_BIN_DIR/../bin/setupCmdLine.sh"`
SKIP_ISCDIR_SETUP=1

setupTDIEnv "$TEMP_BIN_DIR"

"$TDI_JAVA_PROGRAM" $TDI_MIXEDMODE_FLAG -cp "$TDI_HOME_DIR/jars/common/tdiresource.jar:$TDI_HOME_DIR/jars/common/diserverapi.jar:$TDI_HOME_DIR/jars/common/miserver.jar:$TDI_HOME_DIR/jars/common/diserverapirmi.jar:$TDI_HOME_DIR/jars/common/miconfig.jar:$TDI_HOME_DIR/jars/3rdparty/others/log4j-1.2.15.jar:$TDI_HOME_DIR/jars/3rdparty/IBM/icu4j_4_2.jar:$TDI_HOME_DIR/jars/3rdparty/others/mail.jar:$TDI_HOME_DIR/jars/3rdparty/others/activation.jar" com.ibm.di.api.security.CryptoUtils "$@"


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

if [[ -e ./_tdi.rc ]]; then
  rm -f ./_tdi.rc
fi

UNAME_OS=`uname`
if [ "$UNAME_OS" = "OS400" ] ; then
	touch -C 819 ./_tdi.rc
fi
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

. ./tdienv.sh
${TDIPATH}/jvm/jre/bin/java -Xms254M -Xmx1024M $*

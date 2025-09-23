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

if [[ ! -n "$TDIPATH" ]]; then
export TDIPATH=/opt/IBM/TDI/V7.2
fi
if [[ ! -n "$TDI_CS_HOST" ]]; then
export TDI_CS_HOST=localhost
fi
if [[ ! -n "$TDI_CS_PORT" ]]; then
export TDI_CS_PORT=1527
fi

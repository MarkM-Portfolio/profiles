@REM ***************************************************************** 
@REM                                                                   
@REM HCL Confidential                                                  
@REM                                                                   
@REM OCO Source Materials                                              
@REM                                                                   
@REM Copyright HCL Technologies Limited 2009, 2021                     
@REM                                                                   
@REM The source code for this program is not published or otherwise    
@REM divested of its trade secrets, irrespective of what has been      
@REM deposited with the U.S. Copyright Office.                         
@REM                                                                   
@REM ***************************************************************** 

@ECHO OFF
SETLOCAL
CD %~dp0

IF "%1" == "" GOTO SYNTAX
IF NOT "%2" == "" GOTO SYNTAX

IF /I "%1" == "start" (
	SET CMD=start
	GOTO INVOKE
)
IF /I "%1" == "stop" (
	SET CMD=shutdown
	GOTO INVOKE
)
IF /I "%1" == "shutdown" (
	SET CMD=shutdown
	GOTO INVOKE
)
IF /I "%1" == "ping" (
	SET CMD=ping
	GOTO INVOKE
)
IF /I "%1" == "/h" (
	GOTO SYNTAX
)
IF /I "%1" == "-h" (
	GOTO SYNTAX
)
IF /I "%1" == "help" (
	GOTO SYNTAX
)
IF /I "%1" == "help" (
	GOTO SYNTAX
)
ECHO Unrecognized argument: %1

:INVOKE

REM Get TDI variables
CALL .\tdienv.bat
CALL .\derby.bat %CMD% -h %TDI_CS_HOST% -p %TDI_CS_PORT%
GOTO LEAVE

:SYNTAX
echo syntax is netstore start ^| stop ^| ping

:LEAVE
ENDLOCAL

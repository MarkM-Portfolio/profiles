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

@echo off
SETLOCAL
CD %~dp0
set RC=0

REM call common script to set TDI paths
CALL .\TDIENV.bat

REM Start Network Store server if not started already
CALL .\netstore ping >NUL 2>NUL
IF NOT ERRORLEVEL 1 GOTO STOREOK
CALL .\netstore start

:STOREOK

REM set failure code ahead of time in case called program doesn't set anything
echo 1 >".\_tdi.rc"

CALL "%TDIPATH%\ibmdisrv" -s . -c profiles_tdi.xml -r dump_pronounce_to_files
FOR /F "usebackq" %%i in (".\_tdi.rc") DO SET RC=%%i
IF NOT "%RC%" == "0" GOTO FAILPOP
GOTO FINISH

:FAILPOP
ECHO "dump pronunciation from files failed"
GOTO FINISH

:FINISH
ENDLOCAL
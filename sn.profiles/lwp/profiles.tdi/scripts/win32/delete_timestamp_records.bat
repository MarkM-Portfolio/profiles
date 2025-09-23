@REM ***************************************************************** 
@REM                                                                   
@REM HCL Confidential                                                  
@REM                                                                   
@REM OCO Source Materials                                              
@REM                                                                   
@REM Copyright HCL Technologies Limited 2015, 2021                     
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

REM set initial success until success/failure coded (if ever)
echo 0 >".\_tdi.rc"

CALL "%TDIPATH%\ibmdisrv" -s . -c profiles_tdi.xml -r delete_timestamp_records
FOR /F "usebackq" %%i in (".\_tdi.rc") DO SET RC=%%i
IF NOT "%RC%" == "0" GOTO FAILPOP
GOTO FINISH

:FAILPOP
ECHO "delete_timestamp_records failed"
GOTO FINISH

:FINISH
ENDLOCAL & SET RC=%RC%
EXIT /B %RC%

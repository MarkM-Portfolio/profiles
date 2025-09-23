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

SETLOCAL ENABLEEXTENSIONS, ENABLEDELAYEDEXPANSION
CD %~dp0
set RC=0

REM check if the lock file exist
if exist .\sync_all_dns.lck (
   SET RC=1
   goto existFile
)

if "%1" == "" (
   ECHO no parameter, should be sync_interlocki.ilck where i is 1, 2,3, or 4; exiting 
   EXIT /B 1
)

SET file=%1

if exist %file% (
   ECHO sync interlock file - %file% - exists; this is unexptected; exiting.
   EXIT /B 1
)

REM create lock before running
echo syncLock > .\sync_all_dns.lck
ECHO create synchronization lock

REM call common script to set TDI paths
CALL .\TDIENV.bat

REM Start Network Store server if not started already
CALL .\netstore ping >NUL 2>NUL
IF NOT ERRORLEVEL 1 GOTO STOREOK
CALL .\netstore start

:STOREOK

REM set failure code ahead of time in case called program doesn't set anything
echo 1 >"_tdi.rc"

CALL "%TDIPATH%\ibmdisrv" -s . -c profiles_tdi.xml -r sync_all_dns
FOR /F "usebackq" %%i in ("_tdi.rc") DO SET RC=%%i
IF NOT "%RC%" == "0" GOTO FAILPOP
GOTO FINISH

:FAILPOP
ECHO "Synchronize of database repository failed"
GOTO FINISH

:FINISH
CALL .\clearLock.bat
GOTO releaseInterlock

:existFile
ECHO Synchronization Lock file already exist. Please turn off other running sync process before performing this one.

:releaseInterlock:
REM create the file that ends the wait loop in the parent
ECHO releseInterlock>%1
EXIT %RC%
ENDLOCAL & SET RC=%RC%

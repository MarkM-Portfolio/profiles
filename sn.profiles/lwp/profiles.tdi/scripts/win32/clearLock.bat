@echo off
@REM ***************************************************************** 
@REM                                                                   
@REM HCL Confidential                                                  
@REM                                                                   
@REM OCO Source Materials                                              
@REM                                                                   
@REM Copyright HCL Technologies Limited 2010, 2021                     
@REM                                                                   
@REM The source code for this program is not published or otherwise    
@REM divested of its trade secrets, irrespective of what has been      
@REM deposited with the U.S. Copyright Office.                         
@REM                                                                   
@REM ***************************************************************** 
if not exist .\sync_all_dns.lck goto nofile
goto releaseLock

:nofile
ECHO "cannot find sync lock"
GOTO FINISH

:releaseLock
del .\sync_all_dns.lck
ECHO release sync lock
GOTO FINISH

:FINISH
EXIT /B %RC%

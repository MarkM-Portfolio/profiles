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

rem @echo off
SETLOCAL
CD %~dp0

REM Get TDI variables
CALL .\tdienv.bat

SET CLASSPATH=%CLASSPATH%:"%TDIPATH%"\jars\3rdparty\IBM\derby.jar;"%TDIPATH%"\jars\3rdparty\IBM\derbynet.jar
IF /I "%1" == "start" GOTO DOBKGD
CALL "%TDIPATH%\jvm\jre\bin\java" -classpath "%TDIPATH%"\jars\3rdparty\IBM\derby.jar;"%TDIPATH%"\jars\3rdparty\IBM\derbynet.jar org.apache.derby.drda.NetworkServerControl %1 %2 %3 %4 %5 %6 %7
GOTO FINISH

:DOBKGD
START "Network Server" /B "%TDIPATH%\jvm\jre\bin\java" -classpath "%TDIPATH%\jars\3rdparty\IBM\derby.jar;%TDIPATH%\jars\3rdparty\IBM\derbynet.jar" org.apache.derby.drda.NetworkServerControl %1 %2 %3 %4 %5 %6 %7 >logs/netstore.out

:FINISH

ENDLOCAL

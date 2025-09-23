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

@echo off
IF "%TDIPATH%" == "" (
SET TDIPATH=C:\Program Files\IBM\TDI\V7.2
)

IF "%TDI_CS_HOST%" == "" (
SET TDI_CS_HOST=localhost
)

IF "%TDI_CS_PORT%" == "" (
SET TDI_CS_PORT=1527
)

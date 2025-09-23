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
SETLOCAL ENABLEEXTENSIONS, ENABLEDELAYEDEXPANSION

REM call common script to set TDI paths
CALL .\TDIENV.bat

REM check if the lock file exist
IF EXIST .\sync_all_dns.lck (
   ECHO Synchronization Lock file already exists. Please turn off other running sync process before performing this one.
   EXIT /B 1
)

REM create lock before running
ECHO syncLock > .\sync_all_dns.lck
ECHO create synchronization lock

set debug=false

SET me=%~n0
SET parent=%~dp0

IF %debug% == true (
   ECHO debug %debug%
   ECHO me %me%
   ECHO parent %parent%
)


REM determine parent dir name 
SET currentDirWrk=%~p0
SET currentDirWrk=%currentDirWrk:~1,-1%
SET currentDirWrk=%currentDirWrk:\=,%
SET currentDirWrk=%currentDirWrk: =_%
FOR %%a IN (%currentDirWrk%) DO SET "parentDirName=%%a"
SET parentDirName=%parentDirName:_= %

IF %debug% == true (
   ECHO currentDirWrk %currentDirWrk%
   ECHO parentDirName %parentDirName%
)


REM CALL .\clearLock.bat
REM exit /b 0

REM cd to the dir where the script lives
CD %~dp0

set pwd=%cd%

IF %debug% == true (
   ECHO pwd %pwd%
)


SET sync_updates_size_model=single

REM read the relevant properties.  We are mostly interested in:
REM    sync_updates_size_model
REM    sync_updates_hash_partitions
FOR /F "tokens=1,2 delims==" %%A IN (profiles_tdi.properties) DO ( 
   IF "%%A" == "sync_updates_size_model" ( 
      SET sync_updates_size_model=%%B
   ) ELSE (
      IF "%%A" == "sync_updates_hash_partitions" (
         SET sync_updates_hash_partitions=%%B
      ) ELSE (
         IF "%%A" == "sync_updates_working_directory" (
            SET sync_updates_working_directory=%%B
         )
	  )
   )
)

IF %debug% == true (
   ECHO sync_updates_size_model="%sync_updates_size_model%"
   ECHO sync_updates_hash_partitions="%sync_updates_hash_partitions%"
   ECHO sync_updates_working_directory="%sync_updates_working_directory%"
)


REM test for whether we got the properties we want. Note that
REM empty string is considered the same as not existing

REM test for the number of partitions
if "%sync_updates_hash_partitions%" == ""  (
   ECHO "Error: sync_updates_hash_partitions is unset, exiting";
   CALL .\clearLock.bat
   ENDLOCAL
   EXIT /B 1
) ELSE ( 
   IF %debug% == true (
      echo sync_updates_hash_partitions is set to %sync_updates_hash_partitions%
   )
)


REM test for sync_updates_working_directory
if "%sync_updates_working_directory%" == ""  (
   echo Error: sync_updates_working_directory is unset, exiting
   CALL .\clearLock.bat
   ENDLOCAL
   EXIT /B 1
) ELSE ( 
   IF %debug% == true (
      echo sync_updates_working_directory is set to %sync_updates_working_directory%
   )
)

SET sync_updates_dir=%sync_updates_working_directory%
IF %debug% == true (
  echo sync_updates_dir is set to "%sync_updates_dir%"
)

SET model=single
IF %sync_updates_size_model% == multi4 (
    SET model=multi4
    SET /a numberOfJVMs=4
)
IF %sync_updates_size_model% == multi6 (
    SET model=multi6
    SET /a numberOfJVMs=6
)
IF %sync_updates_size_model% == multi8 (
    SET model=multi8
    SET /a numberOfJVMs=8
)

IF %model% == single (
	IF NOT %sync_updates_size_model% == single (
	   ECHO ---------------------------------------  
	   ECHO sync_updates_size_model property value in profiles_tdi.properties is not single, multi4, multi6, or multi8
	   ECHO Will run sync_all_dns in single process
	   ECHO ---------------------------------------  
	)
)
ECHO The model is: %model%

SET /a chkNumPartitions = 0+sync_updates_hash_partitions
IF %debug% == true (
   ECHO chkNumPartitions %chkNumPartitions%
)

IF %chkNumPartitions% == 0 (
   ECHO number of partitions not numerical.  Exiting
   CALL .\clearLock.bat
   ENDLOCAL
   EXIT /B 1
)

REM Delete the return code (*.rc) filed
IF EXIST *.RC (
   DEL /Q *.RC
)

REM always want profiles_tdi_partitions.properties file with property sync_updates_hash_partitions_if_large_model
REM in base solution. Set prop to 0 indicates to run sync all the single way.
ECHO sync_updates_hash_partitions_if_large_model=0 > profiles_tdi_partitions.properties

SET foundoption=false
SET disableTs=false

IF %model% == single (
   ECHO Single JVM model

   FOR %%A IN (%*) DO (
      IF %debug% == true (
         ECHO args: %%A
      )

      REM  if dosab;eTs os set, disable timestamp processing for this invokation
      IF %%A==disableTs (
         SET disableTs=true
         SET foundoption=true
      )

      IF NOT !foundoption!==true (
         ECHO Error: unknown command line argument in single mode: %%A
         ECHO The only valid argument is disableTs
         CALL .\clearLock.bat
         ENDLOCAL
         EXIT /B 1
      )

   )		  

   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties
   
   SET RC=0

   REM Start Network Store server if not started already
   CALL .\netstore ping >NUL 2>NUL
   IF ERRORLEVEL 1 CALL .\netstore start

   REM set failure code ahead of time in case called program doesn't set anything
   ECHO 1 >".\_tdi.rc"

   CALL "%TDIPATH%\ibmdisrv" -s . -c profiles_tdi.xml -r sync_all_dns
   FOR /F "usebackq" %%i in (".\_tdi.rc") DO SET RC=%%i
   IF NOT "!RC!" == "0" (
      ECHO "Synchronize of database repository failed"
   )

   CALL .\clearLock.bat
   ENDLOCAL & SET RC=%RC%
   EXIT /B %RC%
)

CALL .\write_timestamp_file.bat start
SET /p dateTimeStr=<./_tdiTimesamp_start.txt

echo  dateTimeStr: %dateTimeStr%
REM   CALL .\clearLock.bat
REM   EXIT /B 0


SET refreshsols=false
SET cleanlogs=false
SET hashonly=false
SET hashskipdb=false
SET hashskipsrc=false
SET updateonly=false


FOR %%A IN (%*) DO (
   IF %debug% == true (
      ECHO args: %%A
   )

   REM  if disableTs is set, disable timestamp processing for this invoke
   IF %%A==disableTs (
      SET disableTs=true
      SET foundoption=true
   )

   REM  if refreshsols is set, delete the parallel solutions dirs
   REM must do this if one of the files is changed. 
   IF %%A==refreshsols (
      SET refreshsols=true
      SET foundoption=true
   )

   REM if cleanlogs is set, remove all files from logs dirs
   IF %%A==cleanlogs (
      SET cleanlogs=true
      SET foundoption=true
   )

   REM if hashonly is set, exit after generating hash files.  This is for debugging.
   IF %%A==hashonly (
      SET hashonly=true
      SET foundoption=true
   )

   REM skip hashing the db
   IF %%A==hashskipdb (
      SET hashskipdb=true
      SET foundoption=true
   )

   REM skip hashing the source
   IF %%A==hashskipsrc (
      SET hashskipsrc=true
      SET foundoption=true
   )

   REM update phase only
   IF %%A==updateonly (
      SET updateonly=true
      SET foundoption=true
   )

   IF NOT !foundoption!==true (
      ECHO Error: unknown command line argument in multi mode: %%A
      ECHO valid arguments are disableTs, refreshsols, cleanlogs, hashonly, hashskipdb, hashskipsrc, updateonly
      CALL .\clearLock.bat
      ENDLOCAL
      EXIT /B 1
   )

)

   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

   REM make sure 4 setting below don't get in the way
   REM note that the parallel solution dirs should exist
	if %updateonly%==true (
	    ECHO Settting refreshsols, hashonly, hashskipdb, and hashskipsrc to false because updateonly is specified.
	    SET refreshsols=false
	    SET hashonly=false
	    SET hashskipdb=false
	    SET hashskipsrc=false
   )

   IF %debug% == true (
	    ECHO Final values
      ECHO disableTs %disableTs%
      ECHO refreshsols %refreshsols%
      ECHO cleanlogs %cleanlogs%
      ECHO hashonly %hashonly%
      ECHO hashskipdb %hashskipdb%
      ECHO hashskipsrc %hashskipsrc%
      ECHO updateonly %updateonly%
   )

   REM large model, i.e., use multiple jvms/multi-processing
   IF %debug%==true (
      ECHO using large model, i.e., multiple jvms
   )

   SET /a nPartitions=0 + %sync_updates_hash_partitions%

   if %debug%==true (
      echo nPartitions %nPartitions%
   )

   REM  each of the parallel tdi solution directories (or JVM's) needs to know which partitions to 
   REM  process.  Note you cannot change the number (numberOfJVMs) this without changing code! 
   REM  numberOfJVMs is set above based on the model to 4, 6, or 8 

   REM  make sure the number of partitions is a multiple of numberOfJVMs, i.e., 4 at present
   SET /a remainder = %nPartitions% %% %numberOfJVMs%
   SET /a addOffset = %numberOfJVMs% - %remainder%

   IF %remainder% == 0 (
	   SET /a addOffset = 0
   )

   SET /a nPartitions = %nPartitions% + %addOffset%
   if %debug%==true (
      echo numberOfJVMs %numberOfJVMs%
      echo remainder %remainder%
      echo addOffset %addOffset%
   	  echo nPartitions %nPartitions%
   )
   REM EXIT /B 1


   REM get the name of the current directory (just the directory, e.g., TDI)
   SET currDirNameAbs=%pwd%

   if %debug%==true (
      ECHO current directory name;
   	  ECHO %currDirNameAbs%_1
   )

   if %refreshsols%==true (
      if %debug%==true (
         ECHO refreshing parallel sols
         ECHO pwd %cd%
         ECHO %parentDirName%_1
         ECHO ..\%parentDirName%_1
      )

      if EXIST ..\%parentDirName%_1 ( 
         rd /s /q ..\%parentDirName%_1
      )
      if EXIST ..\%parentDirName%_2 ( 
         rd /s /q ..\%parentDirName%_2
      )
      if EXIST ..\%parentDirName%_3 ( 
         rd /s /q ..\%parentDirName%_3
      )
      if EXIST ..\%parentDirName%_4 ( 
         rd /s /q ..\%parentDirName%_4
      )
      if EXIST ..\%parentDirName%_5 ( 
         rd /s /q ..\%parentDirName%_5
      )
      if EXIST ..\%parentDirName%_6 ( 
         rd /s /q ..\%parentDirName%_6
      )
      if EXIST ..\%parentDirName%_7 ( 
         rd /s /q ..\%parentDirName%_7
      )
      if EXIST ..\%parentDirName%_8 ( 
         rd /s /q ..\%parentDirName%_8
      )
   )

   REM go up to the parent directory
   cd ..

   IF %debug% == true (
      echo test that parallel tdi solution dir 1 exists
   )

   IF EXIST %parentDirName%_1 (
      IF %debug% == true (
			ECHO parallel dir _1 exists
      )
	  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_1
	  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_1
	  copy /y %parentDirName%\profiles_functions.js %parentDirName%_1
	  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_1
   ) ELSE (
      IF %debug% == true (
         ECHO create parallel solution dir _1
         ECHO parentDirName %parentDirName%
         ECHO parentDirName_1 %parentDirName%_1
         ECHO pwd %parentDirName%
      )
      MKDIR  %parentDirName%_1
      XCOPY  /S /Q %parentDirName%  %parentDirName%_1
   )

   CD %parentDirName%_1
   IF EXIST *.lck (
	   DEL /Q *.lck
	)

   REM  create props in profiles_tdi_partitions.properties to specify hash db
   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
   ECHO sync_updates_stage=hashdb >> profiles_tdi_partitions.properties
   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

   CD ..


   IF EXIST %parentDirName%_2 (
      IF %debug% == true (
			ECHO parallel dir _2 exists
      )
	  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_2
	  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_2
	  copy /y %parentDirName%\profiles_functions.js %parentDirName%_2
	  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_2
   ) ELSE (
      IF %debug% == true (
         ECHO create parallel solution dir _2
         ECHO parentDirName %parentDirName%
         ECHO parentDirName_2 %parentDirName%_2
         ECHO pwd %parentDirName%
      )
      MKDIR  %parentDirName%_2
      XCOPY  /S /Q %parentDirName%  %parentDirName%_2
   )

   CD %parentDirName%_2
   IF EXIST *.lck (
	   DEL /Q *.lck
	)

   REM  create props in profiles_tdi_partitions.properties to specify hash db
   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
   ECHO sync_updates_stage=hashsrc >> profiles_tdi_partitions.properties
   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

   CD ..


   IF EXIST %parentDirName%_3 (
      IF %debug% == true (
			ECHO parallel dir _3 exists
      )
	  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_3
	  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_3
	  copy /y %parentDirName%\profiles_functions.js %parentDirName%_3
	  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_3
   ) ELSE (
      IF %debug% == true (
         ECHO create parallel solution dir _3
         ECHO parentDirName %parentDirName%
         ECHO parentDirName_3 %parentDirName%_3
         ECHO pwd %parentDirName%
      )
      MKDIR  %parentDirName%_3
      XCOPY  /S /Q %parentDirName%  %parentDirName%_3
   )

   CD %parentDirName%_3
   IF EXIST *.lck (
	   DEL /Q *.lck
	)

   REM  create props in profiles_tdi_partitions.properties to specify update
   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

   CD ..


   IF EXIST %parentDirName%_4 (
      IF %debug% == true (
			ECHO parallel dir _4 exists
      )
	  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_4
	  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_4
	  copy /y %parentDirName%\profiles_functions.js %parentDirName%_4
	  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_4
   ) ELSE (
      IF %debug% == true (
         ECHO create parallel solution dir _4
         ECHO parentDirName %parentDirName%
         ECHO parentDirName_4 %parentDirName%_4
         ECHO pwd %parentDirName%
      )
      MKDIR  %parentDirName%_4
      XCOPY  /S /Q %parentDirName%  %parentDirName%_4
   )

   CD %parentDirName%_4
   IF EXIST *.lck (
	   DEL /Q *.lck
   )

   REM  create props in profiles_tdi_partitions.properties to specify update
   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

   CD ..


   IF %numberOfJVMs% GEQ 6 (
	   IF EXIST %parentDirName%_5 (
	      IF %debug% == true (
				ECHO parallel dir _5 exists
	      )
		  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_5
		  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_5
		  copy /y %parentDirName%\profiles_functions.js %parentDirName%_5
		  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_5
	   ) ELSE (
	      IF %debug% == true (
	         ECHO create parallel solution dir _5
	         ECHO parentDirName %parentDirName%
	         ECHO parentDirName_5 %parentDirName%_5
	         ECHO pwd %parentDirName%
	      )
	      MKDIR  %parentDirName%_5
	      XCOPY  /S /Q %parentDirName%  %parentDirName%_5
	   )

	   CD %parentDirName%_5
	   IF EXIST *.lck (
		   DEL /Q *.lck
	   )

	   REM  create props in profiles_tdi_partitions.properties to specify update
	   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
  	 echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	   CD ..


	   IF EXIST %parentDirName%_6 (
	      IF %debug% == true (
				ECHO parallel dir _6 exists
	      )
		  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_6
		  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_6
		  copy /y %parentDirName%\profiles_functions.js %parentDirName%_6
		  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_6
	   ) ELSE (
	      IF %debug% == true (
	         ECHO create parallel solution dir _6
	         ECHO parentDirName %parentDirName%
	         ECHO parentDirName_6 %parentDirName%_6
	         ECHO pwd %parentDirName%
	      )
	      MKDIR  %parentDirName%_6
	      XCOPY  /S /Q %parentDirName%  %parentDirName%_6
	   )

	   CD %parentDirName%_6
	   IF EXIST *.lck (
		   DEL /Q *.lck
	   )

	   REM  create props in profiles_tdi_partitions.properties to specify update
	   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
	   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	   CD ..
   )

   IF %numberOfJVMs% GEQ 8 (
	   IF EXIST %parentDirName%_7 (
	      IF %debug% == true (
				ECHO parallel dir _7 exists
	      )
		  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_7
		  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_7
		  copy /y %parentDirName%\profiles_functions.js %parentDirName%_7
		  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_7
	   ) ELSE (
	      IF %debug% == true (
	         ECHO create parallel solution dir _7
	         ECHO parentDirName %parentDirName%
	         ECHO parentDirName_7 %parentDirName%_7
	         ECHO pwd %parentDirName%
	      )
	      MKDIR  %parentDirName%_7
	      XCOPY  /S /Q %parentDirName%  %parentDirName%_7
	   )

	   CD %parentDirName%_7
	   IF EXIST *.lck (
		   DEL /Q *.lck
	   )

	   REM  create props in profiles_tdi_partitions.properties to specify update
	   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
	   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	   CD ..


	   IF EXIST %parentDirName%_8 (
	      IF %debug% == true (
				ECHO parallel dir _8 exists
	      )
		  copy /y %parentDirName%\profiles_tdi.properties %parentDirName%_8
		  copy /y %parentDirName%\map_dbrepos_from_source.properties %parentDirName%_8
		  copy /y %parentDirName%\profiles_functions.js %parentDirName%_8
		  copy /y %parentDirName%\collect_ldap_dns_by_chunks.js %parentDirName%_8
	   ) ELSE (
	      IF %debug% == true (
	         ECHO create parallel solution dir _8
	         ECHO parentDirName %parentDirName%
	         ECHO parentDirName_8 %parentDirName%_8
	         ECHO pwd %parentDirName%
	      )
	      MKDIR  %parentDirName%_8
	      XCOPY  /S /Q %parentDirName%  %parentDirName%_8
	   )

	   CD %parentDirName%_8
	   IF EXIST *.lck (
		   DEL /Q *.lck
	   )

	   REM  create props in profiles_tdi_partitions.properties to specify update
	   ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	   ECHO sync_updates_stage=update >> profiles_tdi_partitions.properties
	   echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	   CD ..
   )

   REM don't clean sync_updates_dir if in updateonly mode 
   if NOT %updateonly% ==true (
      IF %debug% == true (
         ECHO clean sync_updates: %cd%
         ECHO starting in: %parentDirName%_1\%sync_updates_dir%\*.* 

      )
      if EXIST %parentDirName%\%sync_updates_dir%\*.* ( 
         DEL /Q %parentDirName%\%sync_updates_dir%\*.*
	  )

         if EXIST .\%parentDirName%_1\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_1\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_2\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_2\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_3\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_3\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_4\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_4\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_5\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_5\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_6\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_6\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_7\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_7\%sync_updates_dir%\*.*
   	     )

         if EXIST .\%parentDirName%_8\%sync_updates_dir%\*.* ( 
            DEL /Q .\%parentDirName%_8\%sync_updates_dir%\*.*
   	     )
   )

REM ECHO past clean sync_updates: .\%parentDirName%_1\*.RC

   IF EXIST .\%parentDirName%_1\*.RC (
      DEL /Q .\%parentDirName%_1\*.RC
   )
   IF EXIST .\%parentDirName%_2\*.RC ( 
      DEL /Q .\%parentDirName%_2\*.RC
   )
   IF EXIST .\%parentDirName%_3\*.RC ( 
      DEL /Q .\%parentDirName%_3\*.RC
   )
   IF EXIST .\%parentDirName%_4\*.RC ( 
      DEL /Q .\%parentDirName%_4\*.RC
   )
   IF EXIST .\%parentDirName%_5\*.RC ( 
      DEL /Q .\%parentDirName%_5\*.RC
   )
   IF EXIST .\%parentDirName%_6\*.RC ( 
      DEL /Q .\%parentDirName%_6\*.RC
   )
   IF EXIST .\%parentDirName%_7\*.RC ( 
      DEL /Q .\%parentDirName%_7\*.RC
   )
   IF EXIST .\%parentDirName%_8\*.RC ( 
      DEL /Q .\%parentDirName%_8\*.RC
   )

   IF %debug% == true (
      ECHO past clean sync_updates
   )

   IF %cleanlogs% == true (
      IF %debug% == true (
         ECHO cleaning logs - .\%parentDirName%_1\logs\*.*
      )

      if EXIST ..\%parentDirName%_1 ( 
         DEL /Q .\%parentDirName%_1\logs\*.*
      )
      if EXIST ..\%parentDirName%_2 ( 
         DEL /Q .\%parentDirName%_2\logs\*.*
      )
      if EXIST ..\%parentDirName%_3 ( 
         DEL /Q .\%parentDirName%_3\logs\*.*
      )
      if EXIST ..\%parentDirName%_4 ( 
         DEL /Q .\%parentDirName%_4\logs\*.*
      )
      if EXIST ..\%parentDirName%_5 ( 
         DEL /Q .\%parentDirName%_5\logs\*.*
      )
      if EXIST ..\%parentDirName%_6 ( 
         DEL /Q .\%parentDirName%_6\logs\*.*
      )
      if EXIST ..\%parentDirName%_7 ( 
         DEL /Q .\%parentDirName%_7\logs\*.*
      )
      if EXIST ..\%parentDirName%_8 ( 
         DEL /Q .\%parentDirName%_8\logs\*.*
      )
   )

	REM go up to the tdisol parent directory, typically TDI.
	cd %parentDirName%

	REM the files below are used to mimic the linux wait command in here in windows.
	REM The strings below are passed to the 4, 6, or 8 sync_all_dns1 processes.  They
	REM point to the sync)wait?.ilck file here in the base directory, and are used
	REM to signal that the process has ended.
	REM 
	SET lock1=..\%parentDirName%\sync_wait1.ilck
	SET lock2=..\%parentDirName%\sync_wait2.ilck
	SET lock3=..\%parentDirName%\sync_wait3.ilck
	SET lock4=..\%parentDirName%\sync_wait4.ilck

	SET lock5=..\%parentDirName%\sync_wait5.ilck
	SET lock6=..\%parentDirName%\sync_wait6.ilck
	SET lock7=..\%parentDirName%\sync_wait7.ilck
	SET lock8=..\%parentDirName%\sync_wait8.ilck

	REM delete all the interlock/wait files
	if exist *.ilck (
	  	DEL /Q *.ilck
	)

	IF %debug% == true (
	   ECHO pausing, about the test updateonly: %updateonly%
	   PAUSE
	)

	REM skip hash if updateonly mode is true
	if %updateonly%==false (
	  REM let's do hashing

      CD ..\!parentDirName!_1

      IF %debug% == true (
         ECHO start hash db: pwd will be: ..\!parentDirName!_1 
         ECHO hashskipdb !hashskipdb!
         PAUSE
      )
      ECHO 1 > _tdihdb.rc
      IF  !hashskipdb! == false (
         IF %debug% == true (
            ECHO got to sync db
         )

         START sync_all_dns1.bat !lock1!

      ) ELSE (
          ECHO releseInterlock > !lock1!
	  )
 
      echo Start hash source
      CD ..\!parentDirName!_2
      ECHO 1 > _tdihsrc.rc
      IF  !hashskipsrc! == false (
      IF %debug% == true (
         ECHO got to sync source
      )
         START sync_all_dns1.bat  !lock2!
      ) ELSE (
          ECHO releseInterlock > !lock2!
	  )

      cd ..\!parentDirName!


	  set /a loopcount = 0
      ECHO Starting wait loop1
:waitloop1
	  set /a loopcount = %loopcount% + 1
	  REM check if it's time to put out message
      if [%loopcount% GEQ 100] (
		  ECHO In wait loop1
		  set /a loopcount = 0
      )

      if not exist !lock1! (
         PING -n 6 127.0.0.1>nul
         goto waitloop1
      )
      if not exist !lock2! (
         PING -n 6 127.0.0.1>nul
         goto waitloop1
      )

      ECHO end wait loop1

      IF  !hashskipsrc! == true (
         echo exiting due to skipped hash source
         CALL .\clearLock.bat
         ENDLOCAL
         EXIT /B 1
      )

      IF  !hashskipdb! == true (
         echo exiting due to skipped hash db
         CALL .\clearLock.bat
         ENDLOCAL
         EXIT /B 1
      )

	   IF %debug% == true (
	      echo renaming the hashing ibmdi.log files	so update won't everwrite
	   )
	   ren ..\!parentDirName!_1\logs\ibmdi.log  ibmdi_hash_db_!dateTimeStr!.log
	   ren ..\!parentDirName!_2\logs\ibmdi.log  ibmdi_hash_scr_!dateTimeStr!.log

	   IF %debug% == true (
	      echo check that hash db worked
	   )

	   SET /p rcdb=<..\!parentDirName!_1/_tdihdb.rc

	   if NOT "!rcdb!" == "0" (
	      echo.
	      echo. 
	      echo Synchronize of Database Repository failed
	      echo Hash of Profiles database failed; exiting
	      echo. 
	      CALL .\clearLock.bat
	      ENDLOCAL
	      exit /B 1
	   )

	   REM check that hash src worked

	   SET /p rcsrc=<..\!parentDirName!_2/_tdihsrc.rc

	    IF NOT "!rcsrc!" == "0" (
	       echo.
	       echo. 
	       echo Synchronize of Source Repository failed
	       echo Hash of Source failed; exiting
	       echo. 
	       CALL .\clearLock.bat
	       ENDLOCAL
	       exit /B 1
	    )

	    IF !debug! == true (
	       echo got by check that both worked
	    )


	    REM copy *.dbids from #1 to #0  i.e., base

	    IF NOT EXIST !sync_updates_dir! (
		   mkdir !sync_updates_dir!
		)


		XCOPY  /S /Q  ..\!parentDirName!_1\!sync_updates_dir!\*.*  !sync_updates_dir!
		del /q ..\!parentDirName!_1\!sync_updates_dir!\*.*

		REM copy *.ldiff from #2 to #0

		XCOPY  /S /Q  ..\!parentDirName!_2\!sync_updates_dir!\*.*  !sync_updates_dir!
		del /q ..\!parentDirName!_2\!sync_updates_dir!\*.*


		XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_1\!sync_updates_dir!
		XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_2\!sync_updates_dir!

		IF NOT EXIST ..\!parentDirName!_3\!sync_updates_dir! (
			mkdir ..\!parentDirName!_3\!sync_updates_dir!
		)
	 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_3\!sync_updates_dir!

		IF NOT EXIST ..\!parentDirName!_4\!sync_updates_dir! (
			mkdir ..\!parentDirName!_4\!sync_updates_dir!
		)
	 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_4\!sync_updates_dir!

		if EXIST ..\%parentDirName%_5 ( 
			IF NOT EXIST ..\!parentDirName!_5\!sync_updates_dir! (
				mkdir ..\!parentDirName!_5\!sync_updates_dir!
			)
		 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_5\!sync_updates_dir!
		)
		if EXIST ..\%parentDirName%_6 ( 
			IF NOT EXIST ..\!parentDirName!_6\!sync_updates_dir! (
				mkdir ..\!parentDirName!_6\!sync_updates_dir!
			)
		 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_6\!sync_updates_dir!
		)
		if EXIST ..\%parentDirName%_7 ( 
			IF NOT EXIST ..\!parentDirName!_7\!sync_updates_dir! (
				mkdir ..\!parentDirName!_7\!sync_updates_dir!
			)
		 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_7\!sync_updates_dir!
		)
		if EXIST ..\%parentDirName%_8 ( 
			IF NOT EXIST ..\!parentDirName!_8\!sync_updates_dir! (
				mkdir ..\!parentDirName!_8\!sync_updates_dir!
			)
		 	XCOPY /S /Q .\!sync_updates_dir!\*.*	..\!parentDirName!_8\!sync_updates_dir!
		)

		if !hashonly! == true (
			echo exiting after hash - hashonly is set
	        CALL .\clearLock.bat
	        ENDLOCAL
			exit /B 0
		)
   )



	IF %debug% == true (
		echo now start update phase
	)


   REM delete the interlock/wait files (again)
   if exist *.ilck (
      DEL /Q *.ilck
   )

	cd ..\%parentDirName%_1
	echo 1 > _tdiupd.rc

	SET /a partitionsPerProcess = %nPartitions% / %numberOfJVMs%
    if %debug%==true (
      echo partitionsPerProcess %partitionsPerProcess%
    )

    REM  set the props in profiles_tdi_partitions.properties, i.e., 
	ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	SET /a countInit=0
	echo sync_updates_count_init=%countInit% >> profiles_tdi_partitions.properties
	SET /a countTo=partitionsPerProcess
	echo sync_updates_count_to=%countTo% >> profiles_tdi_partitions.properties
	echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

    START sync_all_dns1.bat %lock1%

	cd ..\%parentDirName%_2
	echo 1 > _tdiupd.rc

    REM  set the props in profiles_tdi_partitions.properties, i.e., 
	ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	SET /a countInit=0+%countTo%
	echo sync_updates_count_init=%countInit% >> profiles_tdi_partitions.properties
	SET /a countTo=%countTo%+%partitionsPerProcess%
	echo sync_updates_count_to=%countTo% >> profiles_tdi_partitions.properties
	echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

    START sync_all_dns1.bat %lock2%

	cd ..\%parentDirName%_3
	echo 1 > _tdiupd.rc

    REM  set the props in profiles_tdi_partitions.properties, i.e., 
	ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	SET /a countInit=0+%countTo%
	echo sync_updates_count_init=%countInit% >> profiles_tdi_partitions.properties
	SET /a countTo=%countTo%+%partitionsPerProcess%
	echo sync_updates_count_to=%countTo% >> profiles_tdi_partitions.properties
	echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

    START sync_all_dns1.bat %lock3%

	cd ..\%parentDirName%_4
	echo 1 > _tdiupd.rc

    REM  set the props in profiles_tdi_partitions.properties, i.e., 
	ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
	SET /a countInit=0+%countTo%
	echo sync_updates_count_init=%countInit% >> profiles_tdi_partitions.properties
	SET /a countTo=%countTo%+%partitionsPerProcess%
	echo sync_updates_count_to=%countTo% >> profiles_tdi_partitions.properties
	echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

    START sync_all_dns1.bat %lock4%

   IF %numberOfJVMs% GEQ 6 (
		cd ..\%parentDirName%_5
		echo 1 > _tdiupd.rc

	    REM  set the props in profiles_tdi_partitions.properties, i.e., 
		ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
		SET /a countInit=0+!countTo!
		echo sync_updates_count_init=!countInit! >> profiles_tdi_partitions.properties
	    SET /a countTo=!countTo!+%partitionsPerProcess%
		echo sync_updates_count_to=!countTo! >> profiles_tdi_partitions.properties
		echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	    echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	    START sync_all_dns1.bat %lock5%

		cd ..\%parentDirName%_6
		echo 1 > _tdiupd.rc

	    REM  set the props in profiles_tdi_partitions.properties, i.e., 
		ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
		SET /a countInit=0+!countTo!
		echo sync_updates_count_init=!countInit! >> profiles_tdi_partitions.properties
	    SET /a countTo=!countTo!+%partitionsPerProcess%
		echo sync_updates_count_to=!countTo! >> profiles_tdi_partitions.properties
		echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	    echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	    START sync_all_dns1.bat %lock6%
	)

   IF %numberOfJVMs% GEQ 8 (
		cd ..\%parentDirName%_7
		echo 1 > _tdiupd.rc

	    REM  set the props in profiles_tdi_partitions.properties, i.e., 
		ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
		SET /a countInit=0+!countTo!
		echo sync_updates_count_init=!countInit! >> profiles_tdi_partitions.properties
	    SET /a countTo=!countTo!+%partitionsPerProcess%
		echo sync_updates_count_to=!countTo! >> profiles_tdi_partitions.properties
		echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	    echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	    START sync_all_dns1.bat %lock7%

		cd ..\%parentDirName%_8
		echo 1 > _tdiupd.rc

	    REM  set the props in profiles_tdi_partitions.properties, i.e., 
		ECHO sync_updates_hash_partitions_if_large_model=%nPartitions% > profiles_tdi_partitions.properties
		SET /a countInit=0+!countTo!
		echo sync_updates_count_init=!countInit! >> profiles_tdi_partitions.properties
	    SET /a countTo=!countTo!+%partitionsPerProcess%
		echo sync_updates_count_to=!countTo! >> profiles_tdi_partitions.properties
		echo sync_updates_stage=update >> profiles_tdi_partitions.properties
	    echo sync_updates_hash_timestamp_disabled_by_command_arg=!disableTs! >> profiles_tdi_partitions.properties

	    START sync_all_dns1.bat %lock8%
	  )		 


	cd ..\%parentDirName%


set /a loopcount = 0
ECHO Starting wait loop2

:waitloop2
	  set /a loopcount = %loopcount% + 1
	  REM check if it's time to put out message
      if [%loopcount% GEQ 102] (
		  ECHO In wait loop2
		  set /a loopcount = 0
      )

      if not exist !lock1! (
         PING -n 6 127.0.0.1>nul
         goto waitloop2
      )
      if not exist !lock2! (
         PING -n 6 127.0.0.1>nul
         goto waitloop2
      )
      if not exist !lock3! (
         PING -n 6 127.0.0.1>nul
         goto waitloop2
      )
      if not exist !lock4! (
         PING -n 6 127.0.0.1>nul
         goto waitloop2
      )
      IF %numberOfJVMs% GEQ 6 (
	      if not exist !lock5! (
	         PING -n 6 127.0.0.1>nul
	         goto waitloop2
	      )
	      if not exist !lock6! (
	         PING -n 6 127.0.0.1>nul
	         goto waitloop2
	      )
	  )
      IF %numberOfJVMs% GEQ 8 (
	      if not exist !lock7! (
	         PING -n 6 127.0.0.1>nul
	         goto waitloop2
	      )
	      if not exist !lock8! (
	         PING -n 6 127.0.0.1>nul
	         goto waitloop2
	      )
	  )		 

	ECHO end wait loop2

	ren ..\%parentDirName%_1\logs\ibmdi.log  ibmdi_update1_%dateTimeStr%.log
	ren ..\%parentDirName%_2\logs\ibmdi.log  ibmdi_update2_%dateTimeStr%.log
	ren ..\%parentDirName%_3\logs\ibmdi.log  ibmdi_update3_%dateTimeStr%.log
	ren ..\%parentDirName%_4\logs\ibmdi.log  ibmdi_update4_%dateTimeStr%.log

    IF %numberOfJVMs% GEQ 6 (
		ren ..\%parentDirName%_5\logs\ibmdi.log  ibmdi_update5_%dateTimeStr%.log
		ren ..\%parentDirName%_6\logs\ibmdi.log  ibmdi_update6_%dateTimeStr%.log
	)
   IF %numberOfJVMs% GEQ 8 (
		ren ..\%parentDirName%_7\logs\ibmdi.log  ibmdi_update7_%dateTimeStr%.log
		ren ..\%parentDirName%_8\logs\ibmdi.log  ibmdi_update8_%dateTimeStr%.log
	)


REM 	# check final results 1
   SET /p rcupd1=<..\%parentDirName%_1/_tdiupd.rc
   if NOT "%rcupd1%" == "0" (
      echo.
      echo. 
      echo Synchronize of Database Repository failed
      echo Update of batch 1 failed; exiting
      echo. 
      CALL .\clearLock.bat
      ENDLOCAL
      exit /B 1
   )

REM 	# check final results 2
   SET /p rcupd2=<..\%parentDirName%_2/_tdiupd.rc
   if NOT "%rcupd2%" == "0" (
      echo.
      echo. 
      echo Synchronize of Database Repository failed
      echo Update of batch 2 failed; exiting
      echo. 
      CALL .\clearLock.bat
      ENDLOCAL
      exit /B 1
   )

REM 	# check final results 3
   SET /p rcupd3=<..\%parentDirName%_3/_tdiupd.rc
   if NOT "%rcupd3%" == "0" (
      echo.
      echo. 
      echo Synchronize of Database Repository failed
      echo Update of batch 3 failed; exiting
      echo. 
      CALL .\clearLock.bat
      ENDLOCAL
      exit /B 1
   )

REM 	# check final results 4
   SET /p rcupd4=<..\%parentDirName%_4/_tdiupd.rc
   if NOT "%rcupd4%" == "0" (
      echo.
      echo. 
      echo Synchronize of Database Repository failed
      echo Update of batch 4 failed; exiting
      echo. 
      CALL .\clearLock.bat
      ENDLOCAL
      exit /B 1
   )

    IF %numberOfJVMs% GEQ 6 (
		REM 	# check final results 5
		   SET /p rcupd5=<..\!parentDirName!_5/_tdiupd.rc
		   if NOT "!rcupd5!" == "0" (
		      echo.
		      echo. 
		      echo Synchronize of Database Repository failed
		      echo Update of batch 5 failed; exiting
		      echo. 
		      CALL .\clearLock.bat
		      ENDLOCAL
		      exit /B 1
		   )

		REM 	# check final results 6
		   SET /p rcupd6=<..\!parentDirName!_6/_tdiupd.rc
		   if NOT "!rcupd6!" == "0" (
		      echo.
		      echo. 
		      echo Synchronize of Database Repository failed
		      echo Update of batch 6 failed; exiting
		      echo. 
		      CALL .\clearLock.bat
		      ENDLOCAL
		      exit /B 1
		   )
	)

    IF %numberOfJVMs% GEQ 8 (
		REM 	# check final results 7
		   SET /p rcupd7=<..\!parentDirName!_7/_tdiupd.rc
		   if NOT "!rcupd7!" == "0" (
		      echo.
		      echo. 
		      echo Synchronize of Database Repository failed
		      echo Update of batch 7 failed; exiting
		      echo. 
		      CALL .\clearLock.bat
		      ENDLOCAL
		      exit /B 1
		   )

		REM 	# check final results 8
		   SET /p rcupd8=<..\!parentDirName!_8/_tdiupd.rc
		   if NOT "!rcupd8!" == "0" (
		      echo.
		      echo. 
		      echo Synchronize of Database Repository failed
		      echo Update of batch 8 failed; exiting
		      echo. 
		      CALL .\clearLock.bat
		      ENDLOCAL
		      exit /B 1
		   )
	)



   SET /p success_1=<..\%parentDirName%_1\_tdisuccesscount.rc
   SET /p deleted_1=<..\%parentDirName%_1\_tdideletecount.rc
   SET /p unchanged_1=<..\%parentDirName%_1\_tdiduplicatecount.rc
   SET /p failure_1=<..\%parentDirName%_1\_tdifailcount.rc

   SET /p success_2=<..\%parentDirName%_2\_tdisuccesscount.rc
   SET /p deleted_2=<..\%parentDirName%_2\_tdideletecount.rc
   SET /p unchanged_2=<..\%parentDirName%_2\_tdiduplicatecount.rc
   SET /p failure_2=<..\%parentDirName%_2\_tdifailcount.rc

   SET /p success_3=<..\%parentDirName%_3\_tdisuccesscount.rc
   SET /p deleted_3=<..\%parentDirName%_3\_tdideletecount.rc
   SET /p unchanged_3=<..\%parentDirName%_3\_tdiduplicatecount.rc
   SET /p failure_3=<..\%parentDirName%_3\_tdifailcount.rc

   SET /p success_4=<..\%parentDirName%_4\_tdisuccesscount.rc
   SET /p deleted_4=<..\%parentDirName%_4\_tdideletecount.rc
   SET /p unchanged_4=<..\%parentDirName%_4\_tdiduplicatecount.rc
   SET /p failure_4=<..\%parentDirName%_4\_tdifailcount.rc

   SET /a success_5 = 0
   SET /a success_6 = 0
   SET /a success_7 = 0
   SET /a success_8 = 0
   SET /a deleted_5 = 0
   SET /a deleted_6 = 0
   SET /a deleted_7 = 0
   SET /a deleted_8 = 0
   SET /a unchanged_5 = 0
   SET /a unchanged_6 = 0
   SET /a unchanged_7 = 0
   SET /a unchanged_8 = 0
   SET /a failure_5 = 0
   SET /a failure_6 = 0
   SET /a failure_7 = 0
   SET /a failure_8 = 0

    IF %numberOfJVMs% GEQ 6 (
	   SET /p success_5=<..\%parentDirName%_5\_tdisuccesscount.rc
	   SET /p deleted_5=<..\%parentDirName%_5\_tdideletecount.rc
	   SET /p unchanged_5=<..\%parentDirName%_5\_tdiduplicatecount.rc
	   SET /p failure_5=<..\%parentDirName%_5\_tdifailcount.rc

	   SET /p success_6=<..\%parentDirName%_6\_tdisuccesscount.rc
	   SET /p deleted_6=<..\%parentDirName%_6\_tdideletecount.rc
	   SET /p unchanged_6=<..\%parentDirName%_6\_tdiduplicatecount.rc
	   SET /p failure_6=<..\%parentDirName%_6\_tdifailcount.rc
	)

    IF %numberOfJVMs% GEQ 8 (
	   SET /p success_7=<..\%parentDirName%_7\_tdisuccesscount.rc
	   SET /p deleted_7=<..\%parentDirName%_7\_tdideletecount.rc
	   SET /p unchanged_7=<..\%parentDirName%_7\_tdiduplicatecount.rc
	   SET /p failure_7=<..\%parentDirName%_7\_tdifailcount.rc

	   SET /p success_8=<..\%parentDirName%_8\_tdisuccesscount.rc
	   SET /p deleted_8=<..\%parentDirName%_8\_tdideletecount.rc
	   SET /p unchanged_8=<..\%parentDirName%_8\_tdiduplicatecount.rc
	   SET /p failure_8=<..\%parentDirName%_8\_tdifailcount.rc
	)



   SET /a success_total=(success_1 + success_2 + success_3 + success_4 + success_5 + success_6 + success_7 + success_8)
   SET /a deleted_total=(deleted_1 + deleted_2 + deleted_3 + deleted_4 + deleted_5 + deleted_6 + deleted_7 + deleted_8)
   SET /a unchanged_total=(unchanged_1 + unchanged_2 + unchanged_3 + unchanged_4 + unchanged_5 + unchanged_6 + unchanged_7 + unchanged_8)
   SET /a fail_total=(failure_1 + failure_2 + failure_3 + failure_4 + failure_5 + failure_6 + failure_7 + failure_8)

   ECHO.
   ECHO.
   ECHO After synchronzation, the final totals are:
   ECHO     added or modifified records:    %success_total%
   ECHO     deleted or inactivated records: %deleted_total%
   ECHO     unchanged records:              %unchanged_total%
   ECHO     failure records:                %fail_total%
   ECHO.
   ECHO.

   CALL .\clearLock.bat
   ENDLOCAL
   EXIT /B 0





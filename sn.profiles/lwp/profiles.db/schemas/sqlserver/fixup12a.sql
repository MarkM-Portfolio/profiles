-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO


------------------------------------------------
-- DDL Statements for table "EMPINST"."EMPLOYEE"
------------------------------------------------

--- step 1: disable constraints
--- keep the following stmt in sync with integOff.sql

DISABLE TRIGGER ALL ON EMPINST.EMPLOYEE;
GO

exec sp_rename 'EMPINST.EMPLOYEE.EMPLOYEE_PK', 'EMPLOYEE_PK_T'
GO
exec sp_rename 'EMPINST.T_EMPLOYEE_INSRT', 'T_EMPLOYEE_INSRT_T'
GO
exec sp_rename 'EMPINST.T_EMPLOYEE_UPD', 'T_EMPLOYEE_UPD_T'
GO

DROP VIEW EMPINST.SNCORE_PERSON
GO

{include.msgVector-integOff.sql}

--- step 2: rename old table to temp

exec sp_rename 'EMPINST.EMPLOYEE', 'EMPLOYEE_T'
GO

--- step 3b: drop constraints on new table


--- step 3: create new table
--- keep the following stmt in sync with createdb.sql

CREATE TABLE EMPINST.EMPLOYEE  (
		  PROF_KEY 			NVARCHAR(36) NOT NULL,
		  PROF_UID 			NVARCHAR(256) NOT NULL,
		  PROF_UID_LOWER		NVARCHAR(256),
		  PROF_LAST_UPDATE 		DATETIME,
		  PROF_LAST_LOGIN 		DATETIME,

		  PROF_MAIL 			NVARCHAR(256) ,
          	  PROF_MAIL_LOWER 		NVARCHAR(256) ,
		  PROF_GUID 			NVARCHAR(256) NOT NULL,
		  PROF_SOURCE_UID 		NVARCHAR(256) NOT NULL,

          	  PROF_DISPLAY_NAME 		NVARCHAR(256) ,
		  PROF_LOGIN 			NVARCHAR(256),
		  PROF_LOGIN_LOWER		NVARCHAR(256) ,
		  PROF_GIVEN_NAME		NVARCHAR(128)  ,
		  PROF_SURNAME			NVARCHAR(128)  ,
		  PROF_ALTERNATE_LAST_NAME 	NVARCHAR(64) ,
		  PROF_PREFERRED_FIRST_NAME 	NVARCHAR(32) ,
		  PROF_PREFERRED_LAST_NAME 	NVARCHAR(64) ,
		  PROF_TYPE			NVARCHAR(64)  ,

          	  PROF_MANAGER_UID 		NVARCHAR(256) ,
          	  PROF_MANAGER_UID_LOWER	NVARCHAR(256) ,
          	  PROF_SECRETARY_UID 		NVARCHAR(256) ,
          	  PROF_IS_MANAGER 		NCHAR(1) ,

		  PROF_GROUPWARE_EMAIL 		NVARCHAR(256) ,
		  PROF_GW_EMAIL_LOWER 		NVARCHAR(256) ,
		  PROF_JOB_RESPONSIBILITIES 	NVARCHAR(128) ,
          	  PROF_ORGANIZATION_IDENTIFIER NVARCHAR(64) ,
		  PROF_ISO_COUNTRY_CODE 	NVARCHAR(3) ,
		  PROF_FAX_TELEPHONE_NUMBER 	NVARCHAR(32) ,
		  PROF_IP_TELEPHONE_NUMBER 	NVARCHAR(32) ,
		  PROF_MOBILE 			NVARCHAR(32) ,
		  PROF_PAGER 			NVARCHAR(32) ,
		  PROF_TELEPHONE_NUMBER 	NVARCHAR(32) ,
		  PROF_WORK_LOCATION 		NVARCHAR(32) ,

		  PROF_BUILDING_IDENTIFIER 	NVARCHAR(16) ,
		  PROF_DEPARTMENT_NUMBER 	NVARCHAR(16) ,
		  PROF_EMPLOYEE_TYPE 		NVARCHAR(256) ,
		  PROF_FLOOR 			NVARCHAR(16) ,
		  PROF_EMPLOYEE_NUMBER 		NVARCHAR(16) ,
		  PROF_PAGER_TYPE 		NVARCHAR(16) ,
		  PROF_PAGER_ID 		NVARCHAR(32) ,
		  PROF_PAGER_SERVICE_PROVIDER	NVARCHAR(50) ,
		  PROF_PHYSICAL_DELIVERY_OFFICE NVARCHAR(32) ,
		  PROF_PREFERRED_LANGUAGE 	NVARCHAR(100) ,
		  PROF_SHIFT 			NVARCHAR(4) ,
		  PROF_TITLE 			NVARCHAR(256) ,
          	  PROF_COURTESY_TITLE 		NVARCHAR(64) ,
		  PROF_TIMEZONE 		NVARCHAR(50) , 
		  PROF_NATIVE_LAST_NAME 	NVARCHAR(256) ,
		  PROF_NATIVE_FIRST_NAME 	NVARCHAR(256) ,
		  PROF_BLOG_URL 		NVARCHAR(256) ,
		  PROF_FREEBUSY_URL 		NVARCHAR(256) ,
		  PROF_CALENDAR_URL 		NVARCHAR(256) ,

	      	  PROF_DESCRIPTION 		NVARCHAR(max) ,
          	  PROF_EXPERIENCE 		NVARCHAR(max) ,
		  PROF_SOURCE_URL 		NVARCHAR(256)
	  CONSTRAINT EMPLOYEE_PK PRIMARY KEY (PROF_KEY) ) ;
    
GO


-- TO COMPLETE SCHEMA 12 FIXUP, RUN MigrateEmployeeTable JAVA PROGRAM AND THEN FIXUP12B.SQL

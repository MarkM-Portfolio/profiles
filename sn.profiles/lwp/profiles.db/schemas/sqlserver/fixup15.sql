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

DROP TABLE EMPINST.PROF_STRUCT_TAG;
GO

DROP TABLE SNCORE.STRUCT_TAG;
GO

DROP SCHEMA SNCORE;
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE EMPINST.EMPLOYEE DROP COLUMN PROF_LAST_LOGIN;
GO


------------------------------------------------
-- DDL Statements for table "EMPINST"."PHOTO"
------------------------------------------------

DROP INDEX  EMPINST.PHOTO.PHOTO_PK;
ALTER TABLE EMPINST.PHOTO ADD CONSTRAINT PHOTO_PK PRIMARY KEY (PROF_KEY);

------------------------------------------------
-- DDL Statements for table "EMPINST"."PRONUNCIATION"
------------------------------------------------


DROP INDEX  EMPINST.PRONUNCIATION.PRONOUNCE_PK;
ALTER TABLE EMPINST.PRONUNCIATION ADD CONSTRAINT PRONOUNCE_PK PRIMARY KEY (PROF_KEY);


------------------------------------------------
-- DDL Statements for table "EMPINST"."WORKLOC"
------------------------------------------------

DROP INDEX  EMPINST.WORKLOC.WORKLOC_PK;
ALTER TABLE EMPINST.WORKLOC ADD CONSTRAINT WORKLOC_PK PRIMARY KEY (PROF_WORK_LOC);

------------------------------------------------
-- DDL Statements for view "EMPINST"."PROFILE_PREFS"
------------------------------------------------


CREATE TABLE EMPINST.PROFILE_PREFS (
	PROF_KEY			NVARCHAR(36) NOT NULL, 
	PROF_PREFID 			NVARCHAR(128) NOT NULL,
	PROF_VALUE 			NVARCHAR(1024)
  CONSTRAINT PREF_PK PRIMARY KEY (PROF_KEY, PROF_PREFID) 
) ;
GO


------------------------------------------------
-- DDL Statements for view "EMPINST"."PROFILE_LAST_LOGIN"
------------------------------------------------


CREATE TABLE EMPINST.PROFILE_LAST_LOGIN (
	PROF_KEY			NVARCHAR(36) NOT NULL, 
	PROF_LAST_LOGIN			DATETIME 
  CONSTRAINT LAST_LOGIN_PK PRIMARY KEY (PROF_KEY) 
) ;
GO

GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.EVENTLOG TO PROFUSER
GO 
GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.PROFILE_LOGIN TO PROFUSER
GO 
GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.PROFILE_PREFS TO PROFUSER
GO 
GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.PROFILE_LAST_LOGIN TO PROFUSER
GO

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=15 WHERE COMPKEY='Profiles';
GO
UPDATE EMPINST.SNCORE_SCHEMA SET DBSCHEMAVER=2  WHERE COMPKEY='LC_APPEXT_CORE';
GO

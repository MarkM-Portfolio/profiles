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
CREATE ROLE PROFUSER_ROLE;
CREATE USER "PROFUSER" PROFILE "DEFAULT" IDENTIFIED BY "&1" DEFAULT TABLESPACE "PROFREGTABSPACE" TEMPORARY TABLESPACE "TEMP" ACCOUNT UNLOCK;


DROP TABLE EMPINST.PROF_STRUCT_TAG;
DROP VIEW  EMPINST.SNCORE_PERSON;

DROP TABLE  SNCORE.STRUCT_TAG;
DROP USER SNCORE CASCADE;


------------------------------------------------
-- DDL Statements for table "EMPINST"."WORKLOC"
------------------------------------------------

DROP INDEX  EMPINST.WORKLOC_PK;
ALTER TABLE EMPINST.WORKLOC ADD CONSTRAINT WORKLOC_PK PRIMARY KEY (PROF_WORK_LOC);



------------------------------------------------
-- DDL Statements for view "EMPINST"."EVENTLOG"
------------------------------------------------

DROP INDEX "EMPINST"."EVLOG_TYPE_IDX";

CREATE INDEX "EMPINST"."EVLOG_TYPE_IDX" ON EMPINST.EVENTLOG ("CREATED" ASC, "EVENT_TYPE" ASC, "EVENT_KEY" ASC) 
	TABLESPACE PROFINDEXTABSPACE;


------------------------------------------------
-- DDL Statements for table "EMPINST"."PROFILE_LOGIN"
------------------------------------------------

CREATE TABLE EMPINST.PROFILE_LOGIN  (
	PROF_KEY				VARCHAR2(36) NOT NULL, 
	PROF_LOGIN				VARCHAR2(256) NOT NULL,
	CONSTRAINT "LOGIN_PK" PRIMARY KEY ("PROF_KEY", "PROF_LOGIN" )  
) TABLESPACE PROFREGTABSPACE ;

CREATE UNIQUE INDEX EMPINST.LOGIN_UDX ON EMPINST.PROFILE_LOGIN
		(PROF_LOGIN) TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- DDL Statements for view "EMPINST"."PROFILE_PREFS"
------------------------------------------------

CREATE TABLE EMPINST.PROFILE_PREFS (
	"PROF_KEY" 				VARCHAR2(36) NOT NULL, 
	"PROF_PREFID" 				VARCHAR2(128) NOT NULL, 
	"PROF_VALUE"				VARCHAR2(1024),
	CONSTRAINT "PREF_PK" PRIMARY KEY ("PROF_KEY", "PROF_PREFID" )
) TABLESPACE PROFREGTABSPACE ;


------------------------------------------------
-- DDL Statements for view "EMPINST"."PROFILE_LAST_LOGIN"
------------------------------------------------

CREATE TABLE EMPINST.PROFILE_LAST_LOGIN (
	"PROF_KEY" 				VARCHAR2(36) NOT NULL, 
	"PROF_LAST_LOGIN" 			TIMESTAMP NOT NULL, 
	CONSTRAINT "LAST_LOGIN_PK" PRIMARY KEY ("PROF_KEY")
) TABLESPACE PROFREGTABSPACE ;


-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 15 WHERE COMPKEY='Profiles';
UPDATE EMPINST.SNCORE_SCHEMA SET DBSCHEMAVER=  2 WHERE COMPKEY='LC_APPEXT_CORE';

COMMIT;

QUIT;

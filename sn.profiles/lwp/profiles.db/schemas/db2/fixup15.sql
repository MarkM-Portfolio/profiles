-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

UPDATE DATABASE CONFIGURATION USING APPLHEAPSZ 1024@


DROP TABLE EMPINST.PROF_STRUCT_TAG@
DROP VIEW  EMPINST.SNCORE_PERSON@


DROP TABLE  SNCORE.STRUCT_TAG@
DROP SCHEMA SNCORE RESTRICT@



------------------------------------------------
-- DDL Statements for table "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE EMPINST.EMPLOYEE DROP COLUMN PROF_LAST_LOGIN@

REORG TABLE EMPINST.EMPLOYEE use TEMPSPACE32K@

------------------------------------------------
-- DDL Statements for table "EMPINST"."PHOTO"
------------------------------------------------

DROP TRIGGER "EMPINST "."T_PHOTO_INSRT"@ 
DROP TRIGGER "EMPINST "."T_PHOTO_UPDT"@ 

DROP INDEX  EMPINST.PHOTO_PK@
DROP INDEX  EMPINST.PHOTOIDX@

CALL SYSPROC.ALTOBJ ( 'APPLY_CONTINUE_ON_ERROR', 'CREATE TABLE EMPINST.PHOTO ( PROF_KEY VARCHAR (36) NOT NULL , PROF_FILE_TYPE VARCHAR (50) , PROF_UPDATED TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP , PROF_IMAGE BLOB (50000 ) LOGGED NOT COMPACT , PROF_THUMBNAIL BLOB (10000 ) LOGGED NOT COMPACT ) IN USERSPACE4K ', -1, ? )@

ALTER TABLE EMPINST.PHOTO ADD CONSTRAINT PHOTO_PK PRIMARY KEY (PROF_KEY)@


-- DDL Statements for indexes on Table "EMPINST"."PHOTO"

CREATE INDEX "EMPINST"."PHOTOIDX" ON "EMPINST"."PHOTO" 
		("PROF_UPDATED" DESC) ALLOW REVERSE SCANS@


-- DDL Statements for triggers on Table "EMPINST"."PHOTO"

CREATE TRIGGER "EMPINST"."T_PHOTO_INSRT" NO CASCADE BEFORE INSERT ON "EMPINST"."PHOTO" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_UPDATED = CURRENT TIMESTAMP@

CREATE TRIGGER "EMPINST"."T_PHOTO_UPDT" NO CASCADE BEFORE UPDATE ON "EMPINST"."PHOTO" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_UPDATED = CURRENT TIMESTAMP@

------------------------------------------------
-- DDL Statements for table "EMPINST"."PRONUNCIATION"
------------------------------------------------

DROP TRIGGER "EMPINST "."T_PRONOUNCE_INSRT"@ 
DROP TRIGGER "EMPINST "."T_PRONOUNCE_UPDT"@ 

DROP INDEX  EMPINST.PRONOUNCE_PK@
DROP INDEX  EMPINST.PRONOUNCEIDX@


CALL SYSPROC.ALTOBJ ( 'APPLY_CONTINUE_ON_ERROR', 'CREATE TABLE EMPINST.PRONUNCIATION ( PROF_KEY VARCHAR (36) NOT NULL , PROF_PRONOUNCE BLOB (100000 ) LOGGED NOT COMPACT , PROF_UPDATED TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP ) IN USERSPACE4K ', -1, ? )@


ALTER TABLE EMPINST.PRONUNCIATION ADD CONSTRAINT PRONOUNCE_PK PRIMARY KEY (PROF_KEY)@

-- DDL Statements for indexes on Table "EMPINST"."PRONUNCIATION"

CREATE INDEX "EMPINST"."PRONOUNCEIDX" ON "EMPINST"."PRONUNCIATION" 
		("PROF_UPDATED" DESC) ALLOW REVERSE SCANS@

-- DDL Statements for indexes on Table "EMPINST"."PRONUNCIATION"

-- DDL Statements for triggers on Table "EMPINST"."PRONUNCIATION"
CREATE TRIGGER "EMPINST"."T_PRONOUNCE_INSRT" NO CASCADE BEFORE INSERT ON "EMPINST"."PRONUNCIATION" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_UPDATED = CURRENT TIMESTAMP@

CREATE TRIGGER "EMPINST"."T_PRONOUNCE_UPDT" NO CASCADE BEFORE UPDATE ON "EMPINST"."PRONUNCIATION" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_UPDATED = CURRENT TIMESTAMP@
------------------------------------------------
-- DDL Statements for table "EMPINST"."WORKLOC"
------------------------------------------------

DROP INDEX  EMPINST.WORKLOC_PK@
ALTER TABLE EMPINST.WORKLOC ADD CONSTRAINT WORKLOC_PK PRIMARY KEY (PROF_WORK_LOC)@


------------------------------------------------
-- DDL Statements for view "EMPINST"."EVENTLOG"
------------------------------------------------

DROP INDEX "EMPINST"."EVLOG_TYPE_IDX"@

CREATE INDEX "EMPINST"."EVLOG_TYPE_IDX" ON EMPINST.EVENTLOG ("CREATED" ASC, "EVENT_TYPE" ASC, "EVENT_KEY" ASC) ALLOW REVERSE SCANS@

------------------------------------------------
-- DDL Statements for table "EMPINST"."PROFILE_PREFS"
------------------------------------------------

CREATE TABLE "EMPINST"."PROFILE_PREFS"  (
	"PROF_KEY" VARCHAR(36) NOT NULL , 
	"PROF_PREFID" VARCHAR(128) NOT NULL,
	"PROF_VALUE" VARCHAR(1024),
	CONSTRAINT "PREF_PK" PRIMARY KEY ("PROF_KEY", "PROF_PREFID" ) 
) IN USERSPACE4K INDEX IN USERSPACE4K@


------------------------------------------------
-- DDL Statements for table "EMPINST"."PROFILE_LAST_LOGIN"
------------------------------------------------
CREATE TABLE "EMPINST"."PROFILE_LAST_LOGIN"  (
	"PROF_KEY" VARCHAR(36) NOT NULL , 
	"PROF_LAST_LOGIN" TIMESTAMP NOT NULL,
	CONSTRAINT "LAST_LOGIN_PK" PRIMARY KEY ("PROF_KEY") 
) IN USERSPACE4K INDEX IN USERSPACE4K@


-- Update schema versions

reorg table EMPINST.SNPROF_SCHEMA use TEMPSPACE4K@

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 15 WHERE COMPKEY='Profiles'@
UPDATE EMPINST.SNCORE_SCHEMA SET DBSCHEMAVER = 2 WHERE COMPKEY='LC_APPEXT_CORE'@

COMMIT@
CONNECT RESET@

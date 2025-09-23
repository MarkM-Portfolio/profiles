-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

------------------------------------------------
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

CREATE INDEX "EMPINST"."PROF_DISP_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_KEY" ASC, "PROF_DISPLAY_NAME" ASC) ALLOW REVERSE SCANS@

CREATE INDEX "EMPINST"."PROF_SNGN_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_KEY" ASC, "PROF_SURNAME" ASC, "PROF_GIVEN_NAME" ASC) ALLOW REVERSE SCANS@

CREATE INDEX "EMPINST"."PROF_GN2_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_GIVEN_NAME" ASC, "PROF_KEY" ASC) ALLOW REVERSE SCANS@

CREATE INDEX "EMPINST"."PROF_SN2_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_SURNAME" ASC, "PROF_KEY" ASC) ALLOW REVERSE SCANS@

COMMIT@


------------------------------------------------
-- DDL Statements for table "EMPINST"."PHOTO"
------------------------------------------------

DROP TRIGGER "EMPINST "."T_PHOTO_INSRT"@ 
DROP TRIGGER "EMPINST "."T_PHOTO_UPDT"@ 

DROP INDEX  EMPINST.PHOTOIDX@

CALL SYSPROC.ALTOBJ ( 'APPLY_CONTINUE_ON_ERROR', 'CREATE TABLE EMPINST.PHOTO ( PROF_KEY VARCHAR (36) NOT NULL , PROF_FILE_TYPE VARCHAR (50) , PROF_UPDATED TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP , PROF_IMAGE BLOB (50000 ) LOGGED NOT COMPACT , PROF_THUMBNAIL BLOB (10000 ) LOGGED NOT COMPACT ) IN USERSPACE4K ', -1, ? )@

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

DROP INDEX  EMPINST.PRONOUNCEIDX@

CALL SYSPROC.ALTOBJ ( 'APPLY_CONTINUE_ON_ERROR', 'CREATE TABLE EMPINST.PRONUNCIATION ( PROF_KEY VARCHAR (36) NOT NULL , PROF_PRONOUNCE BLOB (100000 ) LOGGED NOT COMPACT , PROF_UPDATED TIMESTAMP NOT NULL WITH DEFAULT CURRENT TIMESTAMP ) IN USERSPACE4K ', -1, ? )@

ALTER TABLE "EMPINST"."PRONUNCIATION" ADD COLUMN "PROF_FILE_TYPE" VARCHAR (50) NOT NULL DEFAULT 'audio/wav'@

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


-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 18, RELEASEVER='2.5.1.0' WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

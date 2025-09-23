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


------------------------------------------------
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE "EMPINST"."EMPLOYEE" ALTER COLUMN "PROF_BUILDING_IDENTIFIER" SET DATA TYPE VARCHAR(64)@
	
CREATE INDEX "EMPINST"."PROF_TYPE_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_TYPE" ASC) ALLOW REVERSE SCANS@

DROP INDEX "EMPINST"."EMP_GUID_IDX"@

CREATE UNIQUE INDEX "EMPINST"."EMP_GUID_UDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_GUID" ASC) ALLOW REVERSE SCANS@

DROP INDEX "EMPINST"."UID_LOWER_IDX"@

CREATE UNIQUE INDEX "EMPINST"."UID_LOWER_UDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_UID_LOWER" ASC) ALLOW REVERSE SCANS@

DROP INDEX "EMPINST"."COUNTRY_UID_IDX"@

CREATE INDEX "EMPINST"."COUNTRY_UID_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_ISO_COUNTRY_CODE", "PROF_KEY")@

DROP INDEX "EMPINST"."JOB_RESP_UID_IDX"@

CREATE INDEX "EMPINST"."JOB_RESP_UID_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_JOB_RESPONSIBILITIES", "PROF_KEY")@

DROP INDEX "EMPINST"."ORG_UID_IDX"@

CREATE INDEX "EMPINST"."ORG_UID_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_ORGANIZATION_IDENTIFIER", "PROF_KEY")@

CREATE INDEX "EMPINST"."MANAGER_UID_LOWER_IDX" ON "EMPINST"."EMPLOYEE"
                ("PROF_MANAGER_UID_LOWER" ASC) ALLOW REVERSE SCANS@

------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

RENAME TABLE "EMPINST"."GIVEN_NAME" TO "GIVEN_NAME_T"@
COMMIT@

CREATE TABLE "EMPINST"."GIVEN_NAME"  (
		  "PROF_KEY" VARCHAR(36) NOT NULL , 
		  "PROF_GIVENNAME" VARCHAR(128) NOT NULL,
		  "PROF_NAME_SOURCE" INTEGER DEFAULT 0 )
		  IN USERSPACE4K INDEX IN USERSPACE4K@


-- copy old data without duplicates 
INSERT INTO "EMPINST"."GIVEN_NAME" (SELECT DISTINCT * FROM "EMPINST"."GIVEN_NAME_T")@


DROP TABLE "EMPINST"."GIVEN_NAME_T"@
COMMIT@

-- DDL Statements for indexes on Table "EMPINST"."GIVEN_NAME"

CREATE INDEX "EMPINST"."GIVEN_NAMEX" ON "EMPINST"."GIVEN_NAME" 
		("PROF_KEY" ASC) @

CREATE INDEX "EMPINST"."GIVEN_NAME_IDX" ON "EMPINST"."GIVEN_NAME" 
		("PROF_GIVENNAME" ASC) @
		
CREATE UNIQUE INDEX "EMPINST"."GIVEN_NAME_UDX" ON "EMPINST"."GIVEN_NAME" 
		("PROF_KEY" ASC, "PROF_GIVENNAME" ASC) ALLOW REVERSE SCANS@


------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------ 

RENAME TABLE "EMPINST"."SURNAME" TO "SURNAME_T"@
COMMIT@

CREATE TABLE "EMPINST"."SURNAME"  (
		  "PROF_KEY" VARCHAR(36) NOT NULL , 
		  "PROF_SURNAME" VARCHAR(128) NOT NULL,
		  "PROF_NAME_SOURCE" INTEGER DEFAULT 0 ) 
		  IN USERSPACE4K INDEX IN USERSPACE4K@


-- copy old data without duplicates 
INSERT INTO "EMPINST"."SURNAME" (SELECT DISTINCT * FROM "EMPINST"."SURNAME_T")@


DROP TABLE "EMPINST"."SURNAME_T"@
COMMIT@

-- DDL Statements for indexes on Table "EMPINST"."SURNAME"

CREATE INDEX "EMPINST"."SURNAMEX" ON "EMPINST"."SURNAME" 
		("PROF_KEY" ASC) @

CREATE INDEX "EMPINST"."SURNAME_IDX" ON "EMPINST"."SURNAME" 
		("PROF_SURNAME" ASC) @

CREATE UNIQUE INDEX "EMPINST"."SURNAME_UDX" ON "EMPINST"."SURNAME" 
		("PROF_KEY" ASC, "PROF_SURNAME" ASC)  ALLOW REVERSE SCANS@


------------------------------------------------
-- DDL Statements for view "EMPINST"."EVENTLOG"
------------------------------------------------

DROP INDEX "EMPINST"."EVLOG_TYPE_IDX"@

CREATE INDEX "EMPINST"."EVLOG_TYPE_IDX" ON EMPINST.EVENTLOG ("EVENT_TYPE" ASC,"CREATED" ASC, "EVENT_KEY" ASC) ALLOW REVERSE SCANS@

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 16 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

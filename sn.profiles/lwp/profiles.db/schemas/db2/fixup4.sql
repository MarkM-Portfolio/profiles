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


--- step 1: disable constraints
--- keep the following stmt in sync with predbxfer.sql

--- step 2: rename old table to temp

RENAME TABLE "EMPINST"."PEOPLE_TAG" TO "PEOPLE_TAG_T"@
COMMIT@

--- step 3: create new table
--- keep the following stmt in sync with createdb.sql

CREATE TABLE "EMPINST "."PEOPLE_TAG" (
	"PROF_TAG_ID" VARCHAR(36) NOT NULL,
	"PROF_SOURCE_KEY" VARCHAR(36) NOT NULL,
	"PROF_TARGET_KEY" VARCHAR(36) NOT NULL,
	"PROF_TAG" VARCHAR(256) NOT NULL,
   CONSTRAINT "PK" PRIMARY KEY (PROF_TAG_ID) )
  IN USERSPACE4K INDEX IN USERSPACE4K@

--- step 4: copy data to new table
INSERT INTO "EMPINST"."PEOPLE_TAG" ("PROF_TAG_ID", "PROF_SOURCE_KEY",  
		"PROF_TARGET_KEY", "PROF_TAG") 
   SELECT 
	CHAR("PROF_TAG_ID"),
	"PROF_TARGET_KEY",
	"PROF_TARGET_KEY",
	"PROF_TAG"
FROM "EMPINST"."PEOPLE_TAG_T"@

COMMIT@

--- step 5: drop temp table

DROP TABLE "EMPINST"."PEOPLE_TAG_T"@
COMMIT@

--- step 6: create triggers/indices new table
--- keep the following stmts in sync with createdb.sql


-- DDL Statements for indexes on Table "EMPINST"."PEOPLE_TAG"


CREATE INDEX "EMPINST"."PEOPLE_TAG_IDX" ON "EMPINST"."PEOPLE_TAG" 
		("PROF_TAG" ASC, "PROF_TARGET_KEY" ASC) @

CREATE INDEX "EMPINST"."PEOPLE_TAG_IDX2" ON "EMPINST"."PEOPLE_TAG" 
		("PROF_TARGET_KEY" ASC) CLUSTER@

CREATE UNIQUE INDEX "EMPINST"."PEOPLE_TAG_UDX" ON "EMPINST"."PEOPLE_TAG" 
		(PROF_SOURCE_KEY ASC, PROF_TARGET_KEY ASC, PROF_TAG ASC) @



------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 4 WHERE COMPKEY='Profiles'@

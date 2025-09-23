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
-- DDL Statements for table "EMPINST"."PEOPLE_TAG"
------------------------------------------------

--- step 1: disable constraints
--- keep the following stmt in sync with predbxfer.sql

--- step 2: rename old table to temp

ALTER TABLE EMPINST.PEOPLE_TAG RENAME TO PEOPLE_TAG_T;


--- step 3: create new table
--- keep the following stmt in sync with createdb.sql

CREATE TABLE EMPINST."PEOPLE_TAG" (
  "PROF_TAG_ID" VARCHAR2(36) NOT NULL ,
  "PROF_SOURCE_KEY" VARCHAR2(36) NOT NULL,
  "PROF_TARGET_KEY" VARCHAR2(36) NOT NULL,
  "PROF_TAG" VARCHAR2(256) NOT NULL,
  PRIMARY KEY (PROF_TAG_ID))  
  ORGANIZATION INDEX
  TABLESPACE PROFREGTABSPACE;

--- step 4: copy data to new table

INSERT INTO "EMPINST"."PEOPLE_TAG" 
	("PROF_TAG_ID",
	"PROF_SOURCE_KEY",
	"PROF_TARGET_KEY", 
	"PROF_TAG") 
  SELECT "PROF_TAG_ID",
	"PROF_TARGET_KEY",
	"PROF_TARGET_KEY",
	"PROF_TAG"
FROM "EMPINST"."PEOPLE_TAG_T";

--- step 5: drop temp table

DROP TABLE EMPINST.PEOPLE_TAG_T;

--- step 6: create triggers/indices new table
--- keep the following stmts in sync with createdb.sql

CREATE INDEX EMPINST."PEOPLE_TAG_IDX" ON EMPINST."PEOPLE_TAG" 
		("PROF_TAG" ASC, "PROF_TARGET_KEY" ASC) TABLESPACE PROFINDEXTABSPACE;


CREATE UNIQUE INDEX EMPINST."PEOPLE_TAG_UDX" ON EMPINST."PEOPLE_TAG" 
		("PROF_SOURCE_KEY" ASC, "PROF_TARGET_KEY" ASC, "PROF_TAG" ASC) TABLESPACE PROFINDEXTABSPACE;


------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 4 WHERE COMPKEY='Profiles';


COMMIT;

QUIT;

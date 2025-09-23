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
-- DDL Statements for table "EMPINST"."SNCOMM_SCHEMA"
------------------------------------------------
CREATE TABLE EMPINST.SNPROF_SCHEMA
  (COMPKEY VARCHAR(36) NOT NULL,
   DBSCHEMAVER INTEGER NOT NULL) IN USERSPACE4K INDEX IN USERSPACE4K@

INSERT INTO EMPINST.SNPROF_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('Profiles', 1)@

-- migrate struct tags

DROP TABLE "SNCORE"."STRUCT_TAG"@

CREATE TABLE "SNCORE"."STRUCT_TAG" (
 "NODE_ID"  CHAR(36) FOR BIT DATA NOT NULL PRIMARY KEY,
 "PARENT_ID"  CHAR(36) FOR BIT DATA,
 "TERM"  VARCHAR (256) NOT NULL,
 "TERM_LOWER" VARCHAR (256) NOT NULL )
  IN USERSPACE4K INDEX IN USERSPACE4K@

-- DDL Statements for indexes on Table "SNCORE "."STRUCT_TAG"

CREATE UNIQUE INDEX "SNCORE "."STRUCT_TAG_UDX" ON "SNCORE"."STRUCT_TAG" 
  ("PARENT_ID", "TERM_LOWER")@

CREATE INDEX "SNCORE "."STRUCT_TAG_PIDX" ON "SNCORE"."STRUCT_TAG" 
  ("PARENT_ID")@


-- migrate prof struct tags

DROP TABLE "EMPINST"."PROF_STRUCT_TAG"@

CREATE TABLE "EMPINST"."PROF_STRUCT_TAG" (
	  "PROF_SOURCE_KEY" VARCHAR(36) NOT NULL,
	  "PROF_TARGET_KEY" VARCHAR(36) NOT NULL,
	  "PROF_NODE_ID" CHAR(36) FOR BIT DATA NOT NULL,
	  CONSTRAINT "PK" PRIMARY KEY (PROF_SOURCE_KEY, PROF_TARGET_KEY, PROF_NODE_ID)) 
	  IN USERSPACE4K INDEX IN USERSPACE4K@

-- DDL Statements for indexes on Table "EMPINST"."PROF_STRUCT_TAG"

CREATE INDEX "EMPINST"."PROF_STRUCT_TAG_IDX" ON "EMPINST"."PROF_STRUCT_TAG" 
		("PROF_TARGET_KEY") CLUSTER@

CREATE INDEX "EMPINST"."PROF_STRUCT_NODE_IDX" ON "EMPINST"."PROF_STRUCT_TAG" 
		("PROF_NODE_ID") @


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
-- DDL Statements for table "EMPINST"."SNCOMM_SCHEMA"
------------------------------------------------
CREATE TABLE EMPINST.SNPROF_SCHEMA
  (COMPKEY VARCHAR2(36) NOT NULL,
   DBSCHEMAVER  CHAR(1)  NOT NULL) TABLESPACE PROFREGTABSPACE;

INSERT INTO EMPINST.SNPROF_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('Profiles', 1);

------------------------------------------------
-- DDL Statements for table "EMPINST"."STRUCT_TAG"
------------------------------------------------
DROP TABLE "SNCORE"."STRUCT_TAG";

CREATE TABLE SNCORE.STRUCT_TAG (
	"NODE_ID"  	CHAR(36)  NOT NULL PRIMARY KEY,
	"PARENT_ID" 	CHAR(36) ,
	"TERM"		VARCHAR2 (256) NOT NULL,
	"TERM_LOWER"	VARCHAR2 (256) NOT NULL)
  TABLESPACE PROFREGTABSPACE;

-- DDL Statements for indexes on Table "EMPINST"."PROF_STRUCT_TAG"
CREATE UNIQUE INDEX SNCORE."STRUCT_TAG_UDX" ON "SNCORE"."STRUCT_TAG" 
  ("PARENT_ID", "TERM_LOWER") TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX SNCORE."STRUCT_TAG_IDX" ON "SNCORE"."STRUCT_TAG" 
  ("PARENT_ID") TABLESPACE PROFINDEXTABSPACE;

GRANT SELECT,UPDATE,DELETE ON SNCORE.STRUCT_TAG TO EMPINST;
		
------------------------------------------------
-- DDL Statements for table "EMPINST"."PROF_STRUCT_TAG"
------------------------------------------------
DROP TABLE "EMPINST"."PROF_STRUCT_TAG";

CREATE TABLE "EMPINST"."PROF_STRUCT_TAG" (
	  "PROF_SOURCE_KEY" VARCHAR2(36) NOT NULL,
	  "PROF_TARGET_KEY" VARCHAR2(36) NOT NULL,
	  "PROF_NODE_ID" CHAR(36) NOT NULL,
	  PRIMARY KEY (PROF_SOURCE_KEY, PROF_TARGET_KEY, PROF_NODE_ID)) 
	  TABLESPACE PROFREGTABSPACE;

-- DDL Statements for indexes on Table "EMPINST"."PROF_STRUCT_TAG"

CREATE INDEX "EMPINST"."PROF_STRUCT_TAG_IDX" ON "EMPINST"."PROF_STRUCT_TAG" 
		("PROF_TARGET_KEY") TABLESPACE PROFINDEXTABSPACE ;

CREATE INDEX "EMPINST"."PROF_STRUCT_NODE_IDX" ON "EMPINST"."PROF_STRUCT_TAG" 
		("PROF_NODE_ID") TABLESPACE PROFINDEXTABSPACE;
		

COMMIT;

QUIT;

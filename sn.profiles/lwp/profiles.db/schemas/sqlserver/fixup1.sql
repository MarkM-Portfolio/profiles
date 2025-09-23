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
-- DDL Statements for table "EMPINST"."SNCOMM_SCHEMA"
------------------------------------------------
CREATE TABLE EMPINST.SNPROF_SCHEMA
  (COMPKEY NVARCHAR(36) NOT NULL,
   DBSCHEMAVER INTEGER NOT NULL);

INSERT INTO EMPINST.SNPROF_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('Profiles', 1);

GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.SNPROF_SCHEMA TO PROFUSER
GO

------------------------------------------------
-- DDL Statements for table SNCORE.STRUCT_TAG
------------------------------------------------

DROP TABLE SNCORE.STRUCT_TAG;

CREATE TABLE SNCORE.STRUCT_TAG (
      	NODE_ID     	CHAR(36) NOT NULL,
	PARENT_ID 	CHAR(36),
	TERM		NVARCHAR (256) NOT NULL,
	TERM_LOWER	NVARCHAR (256));

GO

-- DDL Statements for indexes on Table SNCORE.STRUCT_TAG

CREATE UNIQUE INDEX STRUCT_TAG_UDX ON SNCORE.STRUCT_TAG 
		(PARENT_ID, TERM_LOWER);

GO
CREATE INDEX STRUCT_TAG_IDX ON SNCORE.STRUCT_TAG 
		(PARENT_ID);
GO


------------------------------------------------
-- DDL Statements for table EMPINST.PROF_STRUCT_TAG 
------------------------------------------------

DROP TABLE EMPINST.PROF_STRUCT_TAG;

CREATE TABLE EMPINST.PROF_STRUCT_TAG  (
	PROF_SOURCE_KEY 	NVARCHAR(36) NOT NULL , 
	PROF_TARGET_KEY 	NVARCHAR(36) NOT NULL,
	PROF_NODE_ID 		CHAR(36) NOT NULL
 ) ;
GO

CREATE UNIQUE INDEX PROF_STRUCT_TAG_UDX ON EMPINST.PROF_STRUCT_TAG
		(PROF_SOURCE_KEY ASC, PROF_TARGET_KEY ASC, PROF_NODE_ID ASC) ;
GO

CREATE CLUSTERED INDEX PROF_STRUCT_TAG_IDX ON EMPINST.PROF_STRUCT_TAG
		(PROF_TARGET_KEY ASC) ;
GO

CREATE INDEX PROF_STRUCT_NODE_IDX ON EMPINST.PROF_STRUCT_TAG
		(PROF_NODE_ID ASC) ;
GO

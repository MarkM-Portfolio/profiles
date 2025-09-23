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
-- Upgrade wall to new version
{include.msgVector-fixup3.sql}

-- Update schema version
DROP TABLE EMPINST.SNPROF_SCHEMA;

CREATE TABLE EMPINST.SNPROF_SCHEMA
  (COMPKEY VARCHAR2(36) NOT NULL,
   DBSCHEMAVER  NUMBER(19,0)  NOT NULL) TABLESPACE PROFREGTABSPACE;

INSERT INTO EMPINST.SNPROF_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('Profiles', 10);

COMMIT;

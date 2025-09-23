-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2016
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

------
-- add photo_guid table 
------

CREATE TABLE EMPINST.PHOTO_GUID  (
  PROF_GUID      VARCHAR2(36) NOT NULL , 
  PROF_FILE_TYPE VARCHAR2(50) , 
  PROF_UPDATED   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  PROF_IMAGE     BLOB ,
  PROF_THUMBNAIL BLOB ,
  H_TENANT_KEY   VARCHAR2(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
)
  TABLESPACE PROFREGTABSPACE;

ALTER TABLE EMPINST.PHOTO_GUID ADD CONSTRAINT PHOTO_GUID_PK PRIMARY KEY (PROF_GUID) USING INDEX TABLESPACE PROFINDEXTABSPACE;

COMMIT;

------
-- grant privileges to lcuser. upgrades are supposed to run appGrants.sql, get complaints when pepole forget
------

GRANT INSERT,UPDATE,SELECT,DELETE ON EMPINST.PHOTO_GUID TO PROFUSER_ROLE;

COMMIT;

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER='55.1', RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';

COMMIT;

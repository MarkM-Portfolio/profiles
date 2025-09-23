-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2014                                          
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

--====
-- PHOTO BACKUP
--====

CREATE TABLE EMPINST.PHOTOBKUP  (
  PROF_KEY       VARCHAR2(36) NOT NULL , 
  PROF_FILE_TYPE VARCHAR2(50) , 
  PROF_UPDATED   TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
  PROF_IMAGE     BLOB ,
  PROF_THUMBNAIL BLOB ,
  TENANT_KEY     VARCHAR2(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
) 
  TABLESPACE PROFREGTABSPACE;

ALTER TABLE EMPINST.PHOTOBKUP ADD CONSTRAINT PHOTOBK_PK PRIMARY KEY (PROF_KEY, TENANT_KEY) USING INDEX TABLESPACE PROFINDEXTABSPACE;
COMMIT;

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 46, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

QUIT;
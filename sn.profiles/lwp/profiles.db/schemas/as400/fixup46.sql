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
SET CURRENT SCHEMA EMPINST;

--====
-- PHOTO BACKUP
--====

CREATE TABLE EMPINST.PHOTOBKUP  (
		  PROF_KEY VARCHAR(36) CCSID 1208 NOT NULL , 
		  PROF_FILE_TYPE VARCHAR(50) CCSID 1208 , 
		  PROF_UPDATED TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,
		  PROF_IMAGE BLOB(50000) ,
		  PROF_THUMBNAIL BLOB(10000) ,
		  TENANT_KEY		VARCHAR(36) CCSID 1208 DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
	) ;

ALTER TABLE EMPINST.PHOTOBKUP ADD CONSTRAINT EMPINST.PHOTOBK_PK PRIMARY KEY (PROF_KEY, TENANT_KEY);
COMMIT:

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 46, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

--CONNECT RESET;

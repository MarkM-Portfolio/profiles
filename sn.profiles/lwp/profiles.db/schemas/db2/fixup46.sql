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
CONNECT TO PEOPLEDB@

--====
-- PHOTO BACKUP
--====

CREATE TABLE EMPINST.PHOTOBKUP  (
		  PROF_KEY			VARCHAR(36) NOT NULL , 
		  PROF_FILE_TYPE	VARCHAR(50) , 
		  PROF_UPDATED		TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL,
		  PROF_IMAGE		BLOB(50000) LOGGED NOT COMPACT ,
		  PROF_THUMBNAIL	BLOB(10000) LOGGED NOT COMPACT,
		  TENANT_KEY		VARCHAR(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
 )
	IN USERSPACE4K INDEX IN USERSPACE4K@ 

ALTER TABLE EMPINST.PHOTOBKUP ADD CONSTRAINT PHOTOBK_PK PRIMARY KEY (PROF_KEY, TENANT_KEY)@
COMMIT@

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 46, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------

CONNECT RESET@

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

SET CURRENT SCHEMA EMPINST;

------
-- add photo_guid table 
------

CREATE TABLE EMPINST.PHOTO_GUID (
		  PROF_GUID			VARCHAR(36) CCSID 1208 NOT NULL , 
		  PROF_FILE_TYPE	VARCHAR(50) CCSID 1208 , 
		  PROF_UPDATED		TIMESTAMP DEFAULT CURRENT TIMESTAMP NOT NULL ,
		  PROF_IMAGE		BLOB(50000) ,
		  PROF_THUMBNAIL	BLOB(10000) ,
		  H_TENANT_KEY		VARCHAR(36) CCSID 1208 DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
 ) ;
 
ALTER TABLE EMPINST.PHOTO_GUID ADD CONSTRAINT EMPINST.PHOTO_GUID_PK PRIMARY KEY (PROF_GUID);

COMMIT;

------
-- grant privileges to lcuser. upgrades are supposed to run appGrants.sql, get complaints when pepole forget
------

GRANT DELETE, INSERT, SELECT, UPDATE ON EMPINST.PHOTO_GUID TO LCUSER;

COMMIT;

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER='55.1', RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';

COMMIT;

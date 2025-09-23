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
                                                      
USE PEOPLEDB;

GO
------
-- upgrade-50-55 finished at DBSCHEMAVER = 55, POSTSCHEMAVER = 0.0
-- now apply on-prem versions of postfixup55.1, postfixup55.2, postfixup55.3
------

------
-- postfixup51.sql: add photo_guid table 
------

CREATE TABLE EMPINST.PHOTO_GUID  (
	PROF_GUID	NVARCHAR(36) NOT NULL , 
	PROF_FILE_TYPE	NVARCHAR(50) , 
	PROF_UPDATED	DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
	PROF_IMAGE	VARBINARY(max),
	PROF_THUMBNAIL	VARBINARY(max),
	H_TENANT_KEY	NVARCHAR(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
);
GO

ALTER TABLE EMPINST.PHOTO_GUID ADD CONSTRAINT PHOTO_GUID_PK PRIMARY KEY (PROF_GUID);
GO

------
-- grant privileges to lcuser. upgrades are supposed to run appGrants.sql, get complaints when pepole forget
------

GRANT DELETE,INSERT,SELECT,UPDATE ON EMPINST.PHOTO_GUID TO PROFUSER;
GO

------
-- postfixup55.2.sql on-prem is solely a schema update
------

------
-- postfixup55.3.sql on-prem is solely a schema update
------

------
-- update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER = '55.3', RELEASEVER='6.0.0.0' WHERE COMPKEY='Profiles';

GO

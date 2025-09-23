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
USE PEOPLEDB;
GO

--====
-- EMPLOYEE ROLE MAPPING
--====

CREATE TABLE EMPINST.PHOTOBKUP  (
	PROF_KEY 	NVARCHAR(36) NOT NULL , 
	PROF_FILE_TYPE	NVARCHAR(50) , 
	PROF_UPDATED	DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
	PROF_IMAGE	VARBINARY(max),
	PROF_THUMBNAIL	VARBINARY(max),
	TENANT_KEY	NVARCHAR(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
); 
GO

ALTER TABLE EMPINST.PHOTOBKUP ADD CONSTRAINT PHOTOBK_PK PRIMARY KEY (PROF_KEY, TENANT_KEY);
GO

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 46, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
GO

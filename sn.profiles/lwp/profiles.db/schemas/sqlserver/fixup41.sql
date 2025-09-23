-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2012, 2013                             
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
-- Support a type on a tag
--====
ALTER TABLE EMPINST.PEOPLE_TAG
	ADD PROF_TYPE NVARCHAR(36) NOT NULL DEFAULT 'general';
GO

CREATE INDEX PEOPLE_TAG_IDX3 ON EMPINST.PEOPLE_TAG 
		(PROF_TAG ASC, PROF_TYPE ASC, TENANT_KEY) ;
GO

DROP INDEX PEOPLE_TAG_UDX ON EMPINST.PEOPLE_TAG;
GO

CREATE UNIQUE INDEX PEOPLE_TAG_UDX ON EMPINST.PEOPLE_TAG 
		(PROF_SOURCE_KEY ASC, PROF_TARGET_KEY ASC, PROF_TAG ASC, PROF_TYPE ASC, TENANT_KEY) ;
GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 41, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO
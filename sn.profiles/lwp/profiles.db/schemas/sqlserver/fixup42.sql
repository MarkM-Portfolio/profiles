-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2013                                   
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
-- PROFILE_EXTENSIONS: add TENANT columns and FK constraints
------
ALTER TABLE EMPINST.PROFILE_EXTENSIONS
	ADD SUPPORT_LABEL NUMERIC(5,0) DEFAULT 0 NOT NULL;
GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 42, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO
-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2015                                   
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
-- add home org/tenant key 
------
ALTER TABLE EMPINST.EMPLOYEE ADD H_TENANT_KEY NVARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-040508202233';
GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 52, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
GO
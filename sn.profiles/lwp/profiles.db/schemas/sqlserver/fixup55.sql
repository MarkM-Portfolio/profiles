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
--  add pre/post schema version info
------
ALTER TABLE EMPINST.SNPROF_SCHEMA ADD PRESCHEMAVER NVARCHAR(10) DEFAULT '0' NOT NULL;
ALTER TABLE EMPINST.SNPROF_SCHEMA ADD POSTSCHEMAVER NVARCHAR(10) DEFAULT '0' NOT NULL;
GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 55, RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';
GO
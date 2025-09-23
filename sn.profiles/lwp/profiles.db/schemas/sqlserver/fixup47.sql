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
-- default tenant
--====

update EMPINST.TENANT set TENANT_KEY ='a', TENANT_EXID = 'a' where TENANT_KEY ='00000000-0000-0000-0000-040508202233'; 

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 47, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
GO

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO

ALTER TABLE EMPINST.EMPLOYEE ADD PROF_SOURCE_URL NVARCHAR(256);
GO

------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 3 WHERE COMPKEY='Profiles';
GO

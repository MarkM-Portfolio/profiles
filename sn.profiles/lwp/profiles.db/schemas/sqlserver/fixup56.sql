-- ***************************************************************** 
--                                                                   
-- HCL Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright HCL Technologies Limited 2021, 2022
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 
                                                         
USE PEOPLEDB;
GO

------
-- alter length for WORKLOC.STATE column
------

ALTER TABLE EMPINST.WORKLOC ALTER COLUMN PROF_STATE NVARCHAR(64);
GO

------
-- alter length for EMPLOYEE.PROF_SOURCE_URL column
------

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_SOURCE_URL NVARCHAR(512);
GO

------
-- alter length for EMPLOYEE.PROF_PHYSICAL_DELIVERY_OFFICE column
------

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_PHYSICAL_DELIVERY_OFFICE NVARCHAR(64);
GO

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER = 56, POSTSCHEMAVER='56.0', RELEASEVER='8.0.0.0' WHERE COMPKEY='Profiles';
GO

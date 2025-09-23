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

USE PEOPLEDB;
GO

DISABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

DELETE FROM EMPINST.SNPROF_SCHEMA;
GO
DELETE FROM EMPINST.PROF_CONSTANTS;
GO
DELETE FROM EMPINST.TENANT;
GO

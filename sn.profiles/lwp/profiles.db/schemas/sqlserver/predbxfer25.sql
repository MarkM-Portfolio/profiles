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

USE PEOPLEDB;
GO

DISABLE TRIGGER ALL ON EMPINST.EMPLOYEE;
GO
DISABLE TRIGGER ALL ON EMPINST.PHOTO;
GO
DISABLE TRIGGER ALL ON EMPINST.PRONUNCIATION;
GO
DISABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

DELETE FROM EMPINST.SNPROF_SCHEMA;
GO
DELETE FROM EMPINST.SNCORE_SCHEMA;
GO
DELETE FROM EMPINST.PROF_CONSTANTS;
GO

-- Stuff for wall
{include.msgVector-predbxfer25.sql}

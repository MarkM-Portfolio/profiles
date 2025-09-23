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

ENABLE TRIGGER ALL ON EMPINST.EMPLOYEE;
GO
ENABLE TRIGGER ALL ON EMPINST.PHOTO;
GO
ENABLE TRIGGER ALL ON EMPINST.PRONUNCIATION;
GO
ENABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

-- Stuff for wall
{include.msgVector-postdbxfer25.sql}

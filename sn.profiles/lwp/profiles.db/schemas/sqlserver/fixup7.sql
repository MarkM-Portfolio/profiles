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

------------------------------------------------
-- Stuff to create the wall
------------------------------------------------
{include.msgVector-fixup1.sql}

------------------------------------------------
-- update schema to current version
------------------------------------------------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 7 WHERE COMPKEY='Profiles';
GO

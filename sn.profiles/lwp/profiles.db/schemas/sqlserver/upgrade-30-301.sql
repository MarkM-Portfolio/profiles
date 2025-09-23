-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                        
USE PEOPLEDB
GO

{include.msgVector-fixup6.sql}

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 30, RELEASEVER='3.0.1.0' WHERE COMPKEY='Profiles';
GO

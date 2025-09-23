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

ALTER TABLE EMPINST.EVENTLOG DROP COLUMN EVENT_METADATA;
GO
ALTER TABLE EMPINST.EVENTLOG ADD EVENT_METADATA NVARCHAR(max);
GO

-- includes grants for the wall
{include.msgVector-appGrants.sql}

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=14 WHERE COMPKEY='Profiles';
GO

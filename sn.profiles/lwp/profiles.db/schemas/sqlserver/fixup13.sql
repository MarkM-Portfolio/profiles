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

CREATE INDEX PROFILE_SEARCH_IDX ON EMPINST.EMPLOYEE
        		(PROF_LAST_UPDATE ASC, PROF_KEY ASC);
GO

-- Upgrade wall to new version
{include.msgVector-fixup4.sql}

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=13 WHERE COMPKEY='Profiles';
GO

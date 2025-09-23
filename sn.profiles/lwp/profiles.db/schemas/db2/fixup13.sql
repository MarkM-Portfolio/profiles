-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

DROP INDEX "EMPINST"."PROFILE_SEARCH_IDX"@

CREATE INDEX "EMPINST"."PROFILE_SEARCH_IDX" ON "EMPINST"."EMPLOYEE"
                ("PROF_LAST_UPDATE" ASC, "PROF_KEY" ASC)@

-- Upgrade wall to new version
{include.msgVector-fixup4.sql}

{include.msgVector-reorg.sql}

-- Update schema version
REORG TABLE EMPINST.SNPROF_SCHEMA@
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 13 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

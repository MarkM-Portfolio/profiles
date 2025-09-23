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
-- Upgrade wall to new version
{include.msgVector-fixup2.sql}

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 8 WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

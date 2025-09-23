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

-- Add PK to Employee table
DROP INDEX EMPINST.EMPLOYEE_PK@

ALTER TABLE EMPINST.EMPLOYEE ADD CONSTRAINT EMPLOYEE_PK PRIMARY KEY (PROF_KEY)@

-- Add the wall
{include.msgVector-fixup1.sql}

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 7 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

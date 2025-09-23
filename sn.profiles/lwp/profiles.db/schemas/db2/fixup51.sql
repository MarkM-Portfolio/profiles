-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2014
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

CONNECT TO PEOPLEDB@

------
-- idhash migration (fixup51)
-- There is no fixup50.sql script. There is, instead, a fixup50j.sh script which will run the Java migration app.
-- The Java migration app for EMPLOYEE.PROF_IDHASH expects to find the schema version at 49 and will leave the schema version at 51.
-- This is necessary to avoid re-running the migration app. 
------

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=51, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------

CONNECT RESET@

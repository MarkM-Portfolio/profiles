-- ***************************************************************** 
--                                                                   
-- HCL Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright HCL Technologies Limited 2021, 2022
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

CONNECT TO PEOPLEDB@

------
-- alter length for WORKLOC.STATE column
------


ALTER TABLE EMPINST.WORKLOC ALTER COLUMN PROF_STATE SET DATA TYPE VARCHAR(64)@
COMMIT@

------
-- alter length for EMPLOYEE.PROF_SOURCE_URL column
------

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_SOURCE_URL SET DATA TYPE VARCHAR(512)@
COMMIT@

------
-- alter length for EMPLOYEE.PROF_PHYSICAL_DELIVERY_OFFICE column
------

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_PHYSICAL_DELIVERY_OFFICE SET DATA TYPE VARCHAR(64)@
COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=56, POSTSCHEMAVER='56.0', RELEASEVER='8.0.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------

CONNECT RESET@

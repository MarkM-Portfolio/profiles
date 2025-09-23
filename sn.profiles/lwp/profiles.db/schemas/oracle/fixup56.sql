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



------
-- alter length for WORKLOC.STATE column
------


ALTER TABLE EMPINST.WORKLOC MODIFY PROF_STATE VARCHAR(64);
COMMIT;

------
-- alter length for EMPLOYEE.PROF_SOURCE_URL column
------

ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_SOURCE_URL VARCHAR(512);
COMMIT;

------
-- alter length for EMPLOYEE.PROF_PHYSICAL_DELIVERY_OFFICE column
------

ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_PHYSICAL_DELIVERY_OFFICE VARCHAR(64);
COMMIT;
------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER = 56, POSTSCHEMAVER='56.0', RELEASEVER='8.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

QUIT;

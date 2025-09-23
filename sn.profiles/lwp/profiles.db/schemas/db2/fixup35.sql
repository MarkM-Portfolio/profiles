-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2012, 2013
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

------
-- EMPLOYEE: add index for lookup by DN
------
CREATE INDEX EMPINST.PROF_SRC_UID_IDX ON EMPINST.EMPLOYEE (PROF_SOURCE_UID ASC) ALLOW REVERSE SCANS@
COMMIT@

------
-- PROFILE_EXTENSIONS: drop incorrect constraint that prevented multiple ext attributes
------
--ALTER TABLE EMPINST.PROFILE_EXTENSIONS DROP CONSTRAINT PROFEXT_TENT_UK@
--COMMIT@

------
-- PROFILE_EXT_DRAFT: drop incorrect constraint that prevented multiple ext attributes
------
--ALTER TABLE EMPINST.PROFILE_EXT_DRAFT DROP CONSTRAINT PREXTDRFT_TENT_UK@
--COMMIT@

------
-- Update schema versions
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 35, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------
CONNECT RESET@
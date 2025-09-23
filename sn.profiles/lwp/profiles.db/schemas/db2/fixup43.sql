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

--====
-- EMPLOYEE, NAMES: PROF_MODE for internal/external designation
--====

ALTER TABLE EMPINST.EMPLOYEE
	ADD COLUMN PROF_MODE SMALLINT DEFAULT 0 NOT NULL@

ALTER TABLE EMPINST.GIVEN_NAME
	ADD COLUMN PROF_MODE SMALLINT DEFAULT 0 NOT NULL@

ALTER TABLE EMPINST.SURNAME
	ADD COLUMN PROF_MODE SMALLINT DEFAULT 0 NOT NULL@

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 43, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------
CONNECT RESET@

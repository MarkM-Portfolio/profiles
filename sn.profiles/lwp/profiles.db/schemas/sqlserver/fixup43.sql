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
USE PEOPLEDB;
GO

------
-- EMPLOYEE, NAMES: PROF_MODE for internal/external designation
------
ALTER TABLE EMPINST.EMPLOYEE
	ADD PROF_MODE NUMERIC(5,0) DEFAULT 0 NOT NULL;

ALTER TABLE EMPINST.GIVEN_NAME
	ADD PROF_MODE NUMERIC(5,0) DEFAULT 0 NOT NULL;

ALTER TABLE EMPINST.SURNAME
	ADD PROF_MODE NUMERIC(5,0) DEFAULT 0 NOT NULL;

GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 43, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO
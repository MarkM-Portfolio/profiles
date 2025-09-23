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
USE PEOPLEDB;
GO

------------------------------------------------
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

CREATE INDEX DISP_IDX ON EMPINST.EMPLOYEE 
		(PROF_KEY ASC, PROF_DISPLAY_NAME ASC);
GO

CREATE INDEX PROF_SNGN_IDX ON EMPINST.EMPLOYEE 
		(PROF_KEY ASC, PROF_SURNAME ASC, PROF_GIVEN_NAME ASC);
GO

CREATE INDEX PROF_GN2_IDX ON EMPINST.EMPLOYEE 
		(PROF_GIVEN_NAME ASC, PROF_KEY ASC);
GO

CREATE INDEX PROF_SN2_IDX ON EMPINST.EMPLOYEE 
		(PROF_SURNAME ASC, PROF_KEY ASC);
GO

------------------------------------------------
-- DDL Statements for view "EMPINST"."PRONUNCIATION"
------------------------------------------------

ALTER TABLE EMPINST.PRONUNCIATION ADD PROF_FILE_TYPE NVARCHAR(50) NOT NULL DEFAULT 'audio/wav';
GO

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 18, RELEASEVER='2.5.1.0' WHERE COMPKEY='Profiles';
GO

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

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_LOGIN_LOWER NVARCHAR(256);
GO
------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------ 

DROP TABLE EMPINST.SURNAME_T;
GO

DROP INDEX EMPINST.SURNAME.SURNAME_UDX;
GO

CREATE UNIQUE INDEX SURNAME_UDX ON EMPINST.SURNAME 
		(PROF_KEY ASC, PROF_SURNAME ASC) ;
GO


------------------------------------------------
-- DDL Statements for table EMPINST.PROFILE_EXTENSIONS
------------------------------------------------

CREATE INDEX PROFILE_EXTENSIONS_IDX2 ON EMPINST.PROFILE_EXTENSIONS 
		(PROF_NAME ASC, PROF_PROPERTY_ID ASC);
GO

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 17 WHERE COMPKEY='Profiles';

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO


------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------

DROP INDEX EMPINST.SURNAME.SURNAME_UDX;
GO

DROP INDEX EMPINST.SURNAME.SURNAME_IDX;
GO

ALTER TABLE EMPINST.SURNAME ADD CONSTRAINT SURNAME_PK PRIMARY KEY (PROF_KEY, PROF_SURNAME);
GO

CREATE INDEX SURNAME_IDX ON EMPINST.SURNAME (PROF_SURNAME ASC, PROF_KEY ASC);
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

DROP INDEX EMPINST.GIVEN_NAME.GIVEN_NAME_UDX;

DROP INDEX EMPINST.GIVEN_NAME.GIVEN_NAME_IDX;
GO

ALTER TABLE EMPINST.GIVEN_NAME ADD CONSTRAINT GIVEN_NAME_PK PRIMARY KEY (PROF_KEY, PROF_GIVENNAME);
GO

CREATE INDEX GIVENNAME_IDX ON EMPINST.GIVEN_NAME (PROF_GIVENNAME ASC, PROF_KEY ASC);
GO
 		 
------------------------------------------------
-- DDL Statements for table EMPINST.PROFILE_EXTENSIONS
------------------------------------------------

ALTER TABLE EMPINST.PROFILE_EXTENSIONS ADD CONSTRAINT PROFILE_EXTENSIONS_PK PRIMARY KEY (PROF_KEY, PROF_PROPERTY_ID);
GO

------------------------------------------------
-- DDL Statements for sequence for Table EMPINST.PROFILE_EXT_DRAFT
------------------------------------------------

DROP INDEX  EMPINST.PROFILE_EXT_DRAFT.EXT_DRAFT_PK;
GO

ALTER TABLE EMPINST.PROFILE_EXT_DRAFT ADD CONSTRAINT EXT_DRAFT_PK PRIMARY KEY (PROF_KEY, PROF_UPDATE_SEQUENCE);
GO


------------------------------------------------
-- DDL Statements for sequence for Table EMPINST.PROFILES_SCHEDULER_LMGR
------------------------------------------------

ALTER TABLE EMPINST.PROFILES_SCHEDULER_LMGR
	ADD PRIMARY KEY (LEASENAME);
GO

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 27 WHERE COMPKEY='Profiles';
GO


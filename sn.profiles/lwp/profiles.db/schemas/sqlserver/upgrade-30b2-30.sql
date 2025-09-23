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
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

DROP TRIGGER EMPINST.T_EMPLOYEE_INSRT;
GO

DROP TRIGGER EMPINST.T_EMPLOYEE_UPD;
GO

DROP INDEX EMPINST.EMPLOYEE.PROFILE_SEARCH_IDX;
GO

UPDATE EMPINST.EMPLOYEE SET PROF_LAST_UPDATE=CURRENT_TIMESTAMP WHERE PROF_LAST_UPDATE IS NULL;
ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_LAST_UPDATE DATETIME NOT NULL;
GO

CREATE INDEX PROFILE_SEARCH_IDX ON EMPINST.EMPLOYEE
        		(PROF_LAST_UPDATE ASC, PROF_KEY ASC);
GO

UPDATE EMPINST.EMPLOYEE SET PROF_UID_LOWER=LOWER(PROF_UID);
UPDATE EMPINST.EMPLOYEE SET PROF_MAIL_LOWER=LOWER(PROF_MAIL);
UPDATE EMPINST.EMPLOYEE SET PROF_LOGIN_LOWER=LOWER(PROF_LOGIN);
UPDATE EMPINST.EMPLOYEE SET PROF_MANAGER_UID_LOWER=LOWER(PROF_MANAGER_UID);
UPDATE EMPINST.EMPLOYEE SET PROF_GW_EMAIL_LOWER=LOWER(PROF_GROUPWARE_EMAIL);
GO

DROP INDEX EMPINST.EMPLOYEE.UID_LOWER_UDX;
GO

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_UID_LOWER NVARCHAR(256) NOT NULL;
GO

ALTER TABLE EMPINST.EMPLOYEE ADD PROF_SRC_UID_LOWER NVARCHAR(256);
GO
UPDATE EMPINST.EMPLOYEE SET PROF_SRC_UID_LOWER=LOWER(PROF_SOURCE_UID);
GO
ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_SRC_UID_LOWER NVARCHAR(256) NOT NULL;
GO

CREATE UNIQUE INDEX UID_LOWER_UDX ON EMPINST.EMPLOYEE 
		(PROF_UID_LOWER ASC);
GO

DROP INDEX EMPINST.EMPLOYEE.DISP_IDX;
GO

CREATE INDEX DISP_IDX ON EMPINST.EMPLOYEE 
		(PROF_KEY ASC, PROF_DISPLAY_NAME ASC) INCLUDE (PROF_SURNAME, PROF_GIVEN_NAME);
GO

CREATE INDEX SRC_UID_LOWER_IDX ON EMPINST.EMPLOYEE
        		(PROF_SRC_UID_LOWER ASC, PROF_KEY ASC);
GO

------------------------------------------------
-- DDL Statements for view "EMPINST"."PHOTO"
------------------------------------------------

DROP TRIGGER EMPINST.T_PHOTO_INSRT;
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."PRONUNCIATION"
------------------------------------------------

DROP TRIGGER EMPINST.T_PRONOUNCE_INSRT;
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."EMP_DRAFT"
------------------------------------------------

ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_BUILDING_IDENTIFIER NVARCHAR(64);
GO

ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_DEPARTMENT_NUMBER NVARCHAR(24);
GO

ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_TIMEZONE NVARCHAR(64);
GO

UPDATE EMPINST.EMP_DRAFT SET PROF_LAST_UPDATE=CURRENT_TIMESTAMP WHERE PROF_LAST_UPDATE IS NULL;
GO

ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_LAST_UPDATE DATETIME NOT NULL;
GO

DROP INDEX EMPINST.EMP_DRAFT.ED_PREF_FNX;
DROP INDEX EMPINST.EMP_DRAFT.ED_PREF_LNX;
GO

CREATE INDEX ED_UPDATE_IDX ON EMPINST.EMP_DRAFT 
	(PROF_LAST_UPDATE DESC, PROF_KEY DESC, PROF_UPDATE_SEQUENCE DESC);
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."EVENTLOG"
------------------------------------------------

CREATE INDEX EVLOG_AUDIT_IDX ON EMPINST.EVENTLOG (ISSYSEVENT ASC, EVENT_KEY ASC);



-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 29, RELEASEVER='3.0.0.0' WHERE COMPKEY='Profiles';
GO

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

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_BUILDING_IDENTIFIER NVARCHAR(64);
GO
	
CREATE INDEX PROF_TYPE_IDX ON EMPINST.EMPLOYEE
        		(PROF_TYPE ASC);
GO

DROP INDEX EMPINST.EMPLOYEE.EMP_GUID_IDX;
GO

CREATE UNIQUE INDEX EMP_GUID_UDX ON EMPINST.EMPLOYEE 
		(PROF_GUID ASC);
GO

DROP INDEX EMPINST.EMPLOYEE.UID_LOWER_IDX;

CREATE UNIQUE INDEX UID_LOWER_UDX ON EMPINST.EMPLOYEE 
		(PROF_UID_LOWER ASC);
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

exec sp_rename 'EMPINST.GIVEN_NAME', 'GIVEN_NAME_T'
GO

 
CREATE TABLE EMPINST.GIVEN_NAME  (
		  PROF_KEY 		NVARCHAR(36) NOT NULL , 
		  PROF_GIVENNAME 	NVARCHAR(128) NOT NULL,
		  PROF_NAME_SOURCE NUMERIC(19,0) DEFAULT 0 ) ;
GO


-- copy old data without duplicates 
INSERT INTO EMPINST.GIVEN_NAME (PROF_KEY, PROF_GIVENNAME, PROF_NAME_SOURCE)
(SELECT DISTINCT PROF_KEY, PROF_GIVENNAME, PROF_NAME_SOURCE FROM EMPINST.GIVEN_NAME_T);


-- DDL Statements for indexes on Table EMPINST.GIVEN_NAME

CREATE INDEX GIVEN_NAMEX ON EMPINST.GIVEN_NAME 
		(PROF_KEY ASC) ;
GO

CREATE INDEX GIVEN_NAME_IDX ON EMPINST.GIVEN_NAME 
		(PROF_GIVENNAME ASC) ;
GO
		
CREATE UNIQUE INDEX GIVEN_NAME_UDX ON EMPINST.GIVEN_NAME 
		(PROF_KEY ASC, PROF_GIVENNAME ASC);
GO


DROP TABLE EMPINST.GIVEN_NAME_T;
GO


------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------ 


exec sp_rename 'EMPINST.SURNAME', 'SURNAME_T'
GO

CREATE TABLE EMPINST.SURNAME  (
		  PROF_KEY 		NVARCHAR(36) NOT NULL , 
		  PROF_SURNAME 	NVARCHAR(128) NOT NULL,
		  PROF_NAME_SOURCE NUMERIC(19,0) DEFAULT 0 ) ;
GO


-- copy old data without duplicates 
INSERT INTO EMPINST.SURNAME (PROF_KEY, PROF_SURNAME, PROF_NAME_SOURCE)
(SELECT DISTINCT PROF_KEY, PROF_SURNAME, PROF_NAME_SOURCE FROM EMPINST.SURNAME_T);

-- DDL Statements for indexes on Table EMPINST.SURNAME

CREATE INDEX SURNAMEX ON EMPINST.SURNAME 
		(PROF_KEY ASC) ;
GO

CREATE INDEX SURNAME_IDX ON EMPINST.SURNAME 
		(PROF_SURNAME ASC) ;
GO


CREATE UNIQUE INDEX SURNAME_UDX ON EMPINST.SURNAME 
		(PROF_SURNAME ASC, PROF_KEY ASC) ;
GO

------------------------------------------------
-- DDL Statements for view "EMPINST"."EVENTLOG"
------------------------------------------------


DROP INDEX EMPINST.EVENTLOG.EVLOG_TYPE_IDX;

CREATE INDEX EVLOG_TYPE_IDX ON EMPINST.EVENTLOG (EVENT_TYPE, CREATED, EVENT_KEY);
GO

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 16 WHERE COMPKEY='Profiles';

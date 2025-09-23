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

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_DEPARTMENT_NUMBER NVARCHAR(24);
GO


------------------------------------------------
-- DDL Statements for view "EMPINST"."DEPARTMENT"
------------------------------------------------

exec sp_rename 'EMPINST.DEPARTMENT', 'DEPT_T'
GO

 
 CREATE TABLE EMPINST.DEPARTMENT  (
		  PROF_DEPARTMENT_CODE 	NVARCHAR(24) NOT NULL, 
		  PROF_DEPARTMENT_TITLE 	NVARCHAR(256)) ; 
GO

-- copy old data
INSERT INTO EMPINST.DEPARTMENT (PROF_DEPARTMENT_CODE, PROF_DEPARTMENT_TITLE)
(SELECT DISTINCT PROF_DEPARTMENT_CODE, PROF_DEPARTMENT_TITLE FROM EMPINST.DEPT_T);


-- DDL Statements for indexes on Table EMPINST.DEPARTMENT


ALTER TABLE EMPINST.DEPARTMENT ADD CONSTRAINT DEPT_PK PRIMARY KEY (PROF_DEPARTMENT_CODE ASC);
GO

 CREATE INDEX DEPARTMENT_WIZ1 ON EMPINST.DEPARTMENT 
		(PROF_DEPARTMENT_TITLE ASC);
GO


DROP TABLE EMPINST.DEPT_T;
GO


------------------------------------------------
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS_INDEX"
------------------------------------------------

CREATE TABLE EMPINST.USER_PLATFORM_EVENTS_INDEX (
	EVENT_KEY_INDEX BIGINT NOT NULL DEFAULT 0
) ;
GO


-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 20 WHERE COMPKEY='Profiles';
GO

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2012                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

USE PEOPLEDB;
GO

DISABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

DELETE FROM EMPINST.SNPROF_SCHEMA;
GO
DELETE FROM EMPINST.SNCORE_SCHEMA;
GO
DELETE FROM EMPINST.PROF_CONSTANTS;
GO
DELETE FROM EMPINST.TENANT;
GO

ALTER TABLE EMPINST.EMPLOYEE   DROP CONSTRAINT EMPLOYEE_TENANT_FK;
ALTER TABLE EMPINST.GIVEN_NAME DROP CONSTRAINT GVNAME_TENANT_FK;
ALTER TABLE EMPINST.SURNAME DROP CONSTRAINT SURNAME_TENANT_FK;
ALTER TABLE EMPINST.DEPARTMENT DROP CONSTRAINT DEPT_TENANT_FK;
GO

ALTER TABLE EMPINST.ORGANIZATION DROP CONSTRAINT ORG_TENANT_FK;
ALTER TABLE EMPINST.COUNTRY DROP CONSTRAINT CNTRY_TENANT_FK;
ALTER TABLE EMPINST.EMP_TYPE DROP CONSTRAINT EMPTYPE_TENANT_FK;
ALTER TABLE EMPINST.WORKLOC DROP CONSTRAINT WORKLOC_TENANT_FK;
GO

ALTER TABLE EMPINST.EVENTLOG DROP CONSTRAINT EVLOG_TENANT_FK;
ALTER TABLE EMPINST.PROFILE_LOGIN DROP CONSTRAINT LOGIN_TENANT_FK;
ALTER TABLE EMPINST.USER_PLATFORM_EVENTS DROP CONSTRAINT UPLTEV_TENANT_FK;
GO

{include.msgVector-predbxfer301.sql}

{include.dynattr-predbxfer30.sql}

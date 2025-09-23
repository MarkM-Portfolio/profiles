-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2015
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

USE PEOPLEDB;
GO

TRUNCATE TABLE EMPINST.CHG_EMP_DRAFT;
GO
TRUNCATE TABLE EMPINST.COUNTRY;
GO
TRUNCATE TABLE EMPINST.DEPARTMENT;
GO
TRUNCATE TABLE EMPINST.EMPLOYEE;
GO
TRUNCATE TABLE EMPINST.EMP_DRAFT;
GO
TRUNCATE TABLE EMPINST.EMP_TYPE;
GO
TRUNCATE TABLE EMPINST.GIVEN_NAME;
GO
TRUNCATE TABLE EMPINST.ORGANIZATION;
GO
TRUNCATE TABLE EMPINST.PEOPLE_TAG;
GO
TRUNCATE TABLE EMPINST.PHOTO;
GO
TRUNCATE TABLE EMPINST.PROFILE_EXTENSIONS;
GO
TRUNCATE TABLE EMPINST.PROFILE_EXT_DRAFT;
GO
TRUNCATE TABLE EMPINST.PROF_CONNECTIONS;
GO
TRUNCATE TABLE EMPINST.PRONUNCIATION;
GO
TRUNCATE TABLE EMPINST.SURNAME;
GO
TRUNCATE TABLE EMPINST.WORKLOC;
GO
TRUNCATE TABLE EMPINST.SNPROF_SCHEMA;
GO
DELETE FROM EMPINST.PROF_CONSTANTS;
GO
DELETE FROM EMPINST.EVENTLOG;
GO
DELETE FROM EMPINST.PROFILE_LOGIN;
GO
DELETE FROM EMPINST.PROFILE_PREFS;
GO
DELETE FROM EMPINST.PROFILE_LAST_LOGIN;
GO
DELETE FROM EMPINST.EMP_ROLE_MAP;
GO
DELETE FROM EMPINST.EMP_UPDATE_TIMESTAMP;
GO
DELETE FROM EMPINST.TENANT;
GO


DELETE FROM EMPINST.USER_PLATFORM_EVENTS;
GO
--DELETE FROM EMPINST.USER_PLATFORM_EVENTS_INDEX;
--GO
DELETE FROM EMPINST.PROFILES_SCHEDULER_TASK;
GO
DELETE FROM ON EMPINST.PROFILES_SCHEDULER_LMGR;
GO
DELETE FROM ON EMPINST.PROFILES_SCHEDULER_LMPR;
GO
DELETE FROM ON EMPINST.PROFILES_SCHEDULER_TREG;
GO

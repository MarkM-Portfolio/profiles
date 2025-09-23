-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

USE PEOPLEDB;
GO

UPDATE STATISTICS  EMPINST.GIVEN_NAME;
GO
UPDATE STATISTICS  EMPINST.SURNAME;
GO
UPDATE STATISTICS  EMPINST.PROFILE_EXTENSIONS;
GO
UPDATE STATISTICS  EMPINST.PROFILE_EXT_DRAFT;
GO
UPDATE STATISTICS  EMPINST.PEOPLE_TAG;
GO
UPDATE STATISTICS  EMPINST.DEPARTMENT;
GO
UPDATE STATISTICS  EMPINST.ORGANIZATION;
GO
UPDATE STATISTICS  EMPINST.COUNTRY;
GO
UPDATE STATISTICS  EMPINST.EMP_TYPE;
GO
UPDATE STATISTICS  EMPINST.EMPLOYEE;
GO
UPDATE STATISTICS  EMPINST.PHOTO;
GO
UPDATE STATISTICS  EMPINST.PHOTO_GUID;
GO
UPDATE STATISTICS  EMPINST.PRONUNCIATION;
GO
UPDATE STATISTICS  EMPINST.WORKLOC;
GO
UPDATE STATISTICS  EMPINST.EMP_DRAFT;
GO
UPDATE STATISTICS  EMPINST.CHG_EMP_DRAFT;
GO
UPDATE STATISTICS  EMPINST.PROF_CONNECTIONS;
GO
UPDATE STATISTICS  EMPINST.EVENTLOG;
GO
UPDATE STATISTICS  EMPINST.PROFILE_LOGIN;
GO
UPDATE STATISTICS  EMPINST.PROFILE_PREFS;
GO
UPDATE STATISTICS  EMPINST.PROFILE_LAST_LOGIN;
GO

UPDATE STATISTICS  EMPINST.USER_PLATFORM_EVENTS;
GO
--UPDATE STATISTICS  EMPINST.USER_PLATFORM_EVENTS_INDEX;
--GO
UPDATE STATISTICS  EMPINST.PROFILES_SCHEDULER_TASK;
GO
UPDATE STATISTICS  EMPINST.PROFILES_SCHEDULER_LMGR;
GO
UPDATE STATISTICS  EMPINST.PROFILES_SCHEDULER_LMPR;
GO
UPDATE STATISTICS  EMPINST.PROFILES_SCHEDULER_TREG;
GO


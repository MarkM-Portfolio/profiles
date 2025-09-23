-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2012                                    
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
-- drop obsolete indices from employee table
------

DROP INDEX EMPINST.EMPLOYEE.EMP_UID_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.MAIL_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.SRC_UID_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.GW_EMAIL_LOWER_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.PREF_FNX;
GO

DROP INDEX EMPINST.EMPLOYEE.PREF_LNX;
GO

DROP INDEX EMPINST.EMPLOYEE.MGRIDX;
GO

DROP INDEX EMPINST.EMPLOYEE.GRPEMAIL_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.JOB_RESP_UID_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.ORG_UID_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.COUNTRY_UID_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.FAX_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.IPPHONE_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.MOBILE_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.PAGER_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.PHONE_IDX;
GO

DROP INDEX EMPINST.EMPLOYEE.WORKLOC_IDX;
GO

------
-- drop obsolete indices from other tables
------

DROP INDEX EMPINST.DEPARTMENT.DEPARTMENT_WIZ1;
GO

DROP INDEX EMPINST.ORGANIZATION.ORGANIZATION_WIZ1;
GO

DROP INDEX EMPINST.COUNTRY.COUNTRY_WIZ1;
GO


------
-- add column to store user locale
------

ALTER TABLE EMPINST.PROFILE_LAST_LOGIN ADD PROF_LOCALE NVARCHAR(32);
GO

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 33, RELEASEVER='4.0.0.0' WHERE COMPKEY='Profiles';
GO

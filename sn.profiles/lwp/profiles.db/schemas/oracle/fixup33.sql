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

------
-- drop obsolete indices from employee table
------

DROP INDEX EMPINST.EMP_UID_IDX;
DROP INDEX EMPINST.MAIL_IDX;
DROP INDEX EMPINST.SRC_UID_IDX;
DROP INDEX EMPINST.GW_EMAIL_LOWER_IDX;
DROP INDEX EMPINST.PREF_FNX;
DROP INDEX EMPINST.PREF_LNX;
DROP INDEX EMPINST.MGRIDX;
DROP INDEX EMPINST.GRPEMAIL_IDX;
DROP INDEX EMPINST.JOB_RESP_UID_IDX;
DROP INDEX EMPINST.ORG_UID_IDX;
DROP INDEX EMPINST.COUNTRY_UID_IDX;
DROP INDEX EMPINST.FAX_IDX;
DROP INDEX EMPINST.IPPHONE_IDX;
DROP INDEX EMPINST.MOBILE_IDX;
DROP INDEX EMPINST.PAGER_IDX;
DROP INDEX EMPINST.PHONE_IDX;
DROP INDEX EMPINST.WORKLOC_IDX;
COMMIT;

------
-- drop obsolete indices from other tables
------

DROP INDEX EMPINST.DEPARTMENT_WIZ1;
DROP INDEX EMPINST.ORGANIZATION_WIZ1;
DROP INDEX EMPINST.COUNTRY_WIZ1;
COMMIT;

------
-- add column to store user locale
------
ALTER TABLE EMPINST.PROFILE_LAST_LOGIN ADD PROF_LOCALE VARCHAR2(32);
COMMIT;

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 33, RELEASEVER='4.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

QUIT;

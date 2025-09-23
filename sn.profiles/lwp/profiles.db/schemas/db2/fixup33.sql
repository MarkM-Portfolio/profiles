-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2012, 2013
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

------
-- drop obsolete indices from employee table
------

DROP INDEX "EMPINST"."EMP_UID_IDX"@
COMMIT@

DROP INDEX "EMPINST"."MAIL_IDX"@
COMMIT@

DROP INDEX "EMPINST"."SRC_UID_IDX"@
COMMIT@

DROP INDEX "EMPINST"."GW_EMAIL_LOWER_IDX"@
COMMIT@

DROP INDEX "EMPINST"."PREF_FNX"@
COMMIT@

DROP INDEX "EMPINST"."PREF_LNX"@
COMMIT@

DROP INDEX "EMPINST"."MGRIDX"@
COMMIT@

DROP INDEX "EMPINST"."GRPEMAIL_IDX"@
COMMIT@

DROP INDEX "EMPINST"."JOB_RESP_UID_IDX"@
COMMIT@

DROP INDEX "EMPINST"."ORG_UID_IDX"@
COMMIT@

DROP INDEX "EMPINST"."COUNTRY_UID_IDX"@
COMMIT@

DROP INDEX "EMPINST"."FAX_IDX"@
COMMIT@

DROP INDEX "EMPINST"."IPPHONE_IDX"@
COMMIT@

DROP INDEX "EMPINST"."MOBILE_IDX"@
COMMIT@

DROP INDEX "EMPINST"."PAGER_IDX"@
COMMIT@

DROP INDEX "EMPINST"."PHONE_IDX"@
COMMIT@

DROP INDEX "EMPINST"."WORKLOC_IDX"@
COMMIT@

------
-- drop obsolete indices from other tables
------

DROP INDEX "EMPINST"."DEPARTMENT_WIZ1"@
COMMIT@

DROP INDEX "EMPINST"."ORGANIZATION_WIZ1"@
COMMIT@

DROP INDEX "EMPINST"."COUNTRY_WIZ1"@
COMMIT@


------
-- add column to store user locale
------
ALTER TABLE EMPINST.PROFILE_LAST_LOGIN ADD COLUMN PROF_LOCALE VARCHAR(32)@
COMMIT@


------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 33, RELEASEVER='4.0.0.0' WHERE COMPKEY='Profiles'@
COMMIT@


CONNECT RESET@

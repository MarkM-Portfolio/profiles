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

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@


DROP TRIGGER "EMPINST "."T_EXT_DRAFT_SEQ"@
DROP TRIGGER "EMPINST "."T_EMP_DRAFT_SEQ"@ 
DROP TRIGGER "EMPINST "."T_EMP_INS"@ 
DROP TRIGGER "EMPINST "."T_EMP_DEL"@ 
DROP TRIGGER "EMPINST "."T_EMP_UPD"@
COMMIT@


ALTER TABLE EMPINST.EMPLOYEE      DROP CONSTRAINT EMPLOYEE_TENANT_FK@
ALTER TABLE EMPINST.GIVEN_NAME    DROP CONSTRAINT GVNAME_TENANT_FK@
ALTER TABLE EMPINST.SURNAME       DROP CONSTRAINT SURNAME_TENANT_FK@
ALTER TABLE EMPINST.DEPARTMENT    DROP CONSTRAINT DEPT_TENANT_FK@
ALTER TABLE EMPINST.ORGANIZATION  DROP CONSTRAINT ORG_TENANT_FK@
ALTER TABLE EMPINST.COUNTRY       DROP CONSTRAINT CNTRY_TENANT_FK@
ALTER TABLE EMPINST.EMP_TYPE      DROP CONSTRAINT EMPTYPE_TENANT_FK@
ALTER TABLE EMPINST.WORKLOC       DROP CONSTRAINT WORKLOC_TENANT_FK@
ALTER TABLE EMPINST.EVENTLOG      DROP CONSTRAINT EVLOG_TENANT_FK@
ALTER TABLE EMPINST.PROFILE_LOGIN DROP CONSTRAINT LOGIN_TENANT_FK@
ALTER TABLE EMPINST.USER_PLATFORM_EVENTS DROP CONSTRAINT UPLTEV_TENANT_FK@
COMMIT@

{include.msgVector-predbxfer301.sql}

{include.dynattr-predbxfer30.sql}

DELETE FROM EMPINST.SNPROF_SCHEMA@
DELETE FROM EMPINST.SNCORE_SCHEMA@
DELETE FROM EMPINST.PROF_CONSTANTS@
DELETE FROM EMPINST.TENANT@

COMMIT@
CONNECT RESET@

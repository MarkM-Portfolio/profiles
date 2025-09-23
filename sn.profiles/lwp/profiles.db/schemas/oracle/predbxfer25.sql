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
-- DROP ALL TRIGGERS --------
DROP TRIGGER EMPINST."T_EXT_DRAFT_SEQ";
DROP TRIGGER EMPINST.T_EMPLOYEE_INSRT;
DROP TRIGGER EMPINST."T_EMPLOYEE_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UID_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_GW_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_LOGIN_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MANAGER_UID_UPDT";
DROP TRIGGER EMPINST."T_PHOTO_INSRT";
DROP TRIGGER EMPINST."T_PHOTO_UPDT";
DROP TRIGGER EMPINST."T_PRONOUNCE_INSRT";
DROP TRIGGER EMPINST."T_PRONOUNCE_UPDT";
DROP TRIGGER EMPINST."T_EMP_DRAFT_SEQ";
DROP TRIGGER EMPINST."T_EMP_INS";
DROP TRIGGER EMPINST."T_EMP_DEL";
DROP TRIGGER EMPINST."T_EMP_UPD";

-- DROP createDb inserted values
DELETE FROM EMPINST.SNCORE_SCHEMA;
DELETE FROM EMPINST.PROF_CONSTANTS;

DROP TABLE EMPINST.SNPROF_SCHEMA;

CREATE TABLE EMPINST.SNPROF_SCHEMA
  (COMPKEY VARCHAR2(36) NOT NULL,
   DBSCHEMAVER  NUMBER(19,0)  NOT NULL,
   RELEASEVER VARCHAR2(32)) TABLESPACE PROFREGTABSPACE;

GRANT INSERT,UPDATE,SELECT,DELETE ON EMPINST.SNPROF_SCHEMA TO PROFUSER_ROLE;


-- MSG Vector pre-work
{include.msgVector-predbxfer25.sql}

COMMIT;
QUIT;

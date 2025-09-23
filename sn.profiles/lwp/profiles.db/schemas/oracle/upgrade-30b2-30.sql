-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          

------------------------------------------------
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

DROP TRIGGER EMPINST."T_EMPLOYEE_INSRT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UID_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_GW_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_LOGIN_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MANAGER_UID_UPDT";

DROP INDEX EMPINST.DISP_IDX;

CREATE INDEX "EMPINST"."DISP_IDX" ON "EMPINST"."EMPLOYEE" 
	("PROF_KEY" ASC, "PROF_DISPLAY_NAME" ASC, "PROF_SURNAME" ASC, "PROF_GIVEN_NAME" ASC)
	TABLESPACE PROFINDEXTABSPACE;

UPDATE EMPINST.EMPLOYEE SET PROF_LAST_UPDATE=CURRENT_TIMESTAMP WHERE PROF_LAST_UPDATE IS NULL;
ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_LAST_UPDATE NOT NULL;

UPDATE EMPINST.EMPLOYEE SET PROF_UID_LOWER=LOWER(PROF_UID);
UPDATE EMPINST.EMPLOYEE SET PROF_MAIL_LOWER=LOWER(PROF_MAIL);
UPDATE EMPINST.EMPLOYEE SET PROF_LOGIN_LOWER=LOWER(PROF_LOGIN);
UPDATE EMPINST.EMPLOYEE SET PROF_MANAGER_UID_LOWER=LOWER(PROF_MANAGER_UID);
UPDATE EMPINST.EMPLOYEE SET PROF_GW_EMAIL_LOWER=LOWER(PROF_GROUPWARE_EMAIL);

ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_UID_LOWER NOT NULL;
ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_SOURCE_UID NOT NULL;

ALTER TABLE EMPINST.EMPLOYEE ADD PROF_SRC_UID_LOWER VARCHAR2(256);
UPDATE EMPINST.EMPLOYEE SET PROF_SRC_UID_LOWER=LOWER(PROF_SOURCE_UID);
ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_SRC_UID_LOWER NOT NULL;

CREATE INDEX "EMPINST"."SRC_UID_LOWER_IDX" ON "EMPINST"."EMPLOYEE" 
		("PROF_SRC_UID_LOWER" ASC, "PROF_KEY" ASC) TABLESPACE PROFINDEXTABSPACE;


------------------------------------------------
-- DDL Statements for table "EMPINST"."EMP_DRAFT"
------------------------------------------------

ALTER TABLE "EMPINST"."EMP_DRAFT" MODIFY "PROF_DEPARTMENT_NUMBER" VARCHAR2(24);
ALTER TABLE "EMPINST"."EMP_DRAFT" MODIFY "PROF_TIMEZONE" VARCHAR2(64);

UPDATE EMPINST.EMP_DRAFT SET PROF_LAST_UPDATE=CURRENT_TIMESTAMP WHERE PROF_LAST_UPDATE IS NULL;

ALTER TABLE EMPINST.EMP_DRAFT MODIFY PROF_LAST_UPDATE NOT NULL;

DROP INDEX EMPINST.ED_PREF_FNX;
DROP INDEX EMPINST.ED_PREF_LNX;


CREATE INDEX ED_UPDATE_IDX ON EMPINST.EMP_DRAFT 
	(PROF_LAST_UPDATE DESC, PROF_KEY DESC, PROF_UPDATE_SEQUENCE DESC) TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- DDL Statements for view "EMPINST"."PHOTO"
------------------------------------------------

DROP TRIGGER "EMPINST"."T_PHOTO_INSRT";

DROP TRIGGER "EMPINST"."T_PHOTO_UPDT";

------------------------------------------------
-- DDL Statements for table "EMPINST"."PRONUNCIATION"
------------------------------------------------

DROP TRIGGER "EMPINST"."T_PRONOUNCE_INSRT";

DROP TRIGGER "EMPINST"."T_PRONOUNCE_UPDT";

------------------------------------------------
-- DDL Statements for table "EMPINST"."EVENTLOG"
------------------------------------------------

CREATE INDEX EVLOG_AUDIT_IDX ON EMPINST.EVENTLOG (ISSYSEVENT ASC, EVENT_KEY ASC) TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- DDL Statements for table "EMPINST"."PEOPLE_TAG"
------------------------------------------------

CREATE INDEX EMPINST."PEOPLE_TAG_IDX2" ON EMPINST."PEOPLE_TAG" 
	("PROF_TARGET_KEY" ASC, "PROF_TAG" ASC, "PROF_SOURCE_KEY" ASC) TABLESPACE PROFINDEXTABSPACE;

-- compile triggers
ALTER TRIGGER EMPINST.T_EMP_DEL       COMPILE;
ALTER TRIGGER EMPINST.T_EMP_DRAFT_SEQ COMPILE;
ALTER TRIGGER EMPINST.T_EMP_INS       COMPILE;
ALTER TRIGGER EMPINST.T_EMP_UPD       COMPILE;
ALTER TRIGGER EMPINST.T_EXT_DRAFT_SEQ COMPILE;

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 29 WHERE COMPKEY='Profiles';

COMMIT;
QUIT;

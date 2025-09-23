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
-- DDL Statements for table "EMPINST"."EMPLOYEE"
------------------------------------------------


-- DROP ALL TRIGGERS --------
DROP TRIGGER EMPINST."T_EMPLOYEE_INSRT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UID_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_GW_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_LOGIN_UPDT";
------------------------------

DROP VIEW EMPINST.SNCORE_PERSON;

{include.msgVector-predbxfer.sql}

--- step 1: disable constraints
--- keep the following stmt in sync with predbxfer.sql

ALTER TABLE EMPINST.EMPLOYEE DROP CONSTRAINT EMPLOYEE_PK;

--- step 2: rename old table to temp

ALTER TABLE EMPINST.EMPLOYEE RENAME TO EMPLOYEE_T;

--- step 3a: create new table
--- keep the following stmt in sync with createdb.sql

CREATE TABLE EMPINST."EMPLOYEE"  (
  "PROF_KEY" VARCHAR2(36) NOT NULL,
  "PROF_UID" VARCHAR2(256) NOT NULL ,
  "PROF_UID_LOWER" VARCHAR2(256)  ,
  "PROF_LAST_UPDATE" TIMESTAMP ,
  "PROF_LAST_LOGIN" TIMESTAMP ,
  "PROF_MAIL" VARCHAR2(64) ,
  "PROF_MAIL_LOWER" VARCHAR2(64) ,
  "PROF_GUID" VARCHAR2(256) NOT NULL,
  "PROF_SOURCE_UID" VARCHAR2(256) ,
  "PROF_DISPLAY_NAME" VARCHAR2(256) ,
  "PROF_LOGIN" VARCHAR2(36) ,
  "PROF_LOGIN_LOWER" VARCHAR2(36) ,
  "PROF_GIVEN_NAME"		VARCHAR2(128)  ,
  "PROF_SURNAME"		VARCHAR2(128) , 
  "PROF_ALTERNATE_LAST_NAME" VARCHAR2(64) ,
  "PROF_PREFERRED_FIRST_NAME" VARCHAR2(32) ,
  "PROF_PREFERRED_LAST_NAME" VARCHAR2(64) ,
  "PROF_TYPE"			VARCHAR2(64),
  "PROF_MANAGER_UID" VARCHAR2(256) ,
  "PROF_MANAGER_UID_LOWER" VARCHAR2(256) ,
  "PROF_SECRETARY_UID" VARCHAR2(256) ,
  "PROF_IS_MANAGER" CHAR(1) ,
  "PROF_GROUPWARE_EMAIL" VARCHAR2(128) ,
  "PROF_GW_EMAIL_LOWER" VARCHAR2(128) ,
  "PROF_JOB_RESPONSIBILITIES" VARCHAR2(128) ,
  "PROF_ORGANIZATION_IDENTIFIER" VARCHAR2(64) ,
  "PROF_ISO_COUNTRY_CODE" VARCHAR2(3) ,
  "PROF_FAX_TELEPHONE_NUMBER" VARCHAR2(32) ,
  "PROF_IP_TELEPHONE_NUMBER" VARCHAR2(32),
  "PROF_MOBILE" VARCHAR2(32) ,
  "PROF_PAGER" VARCHAR2(32) ,
  "PROF_TELEPHONE_NUMBER" VARCHAR2(32) ,
  "PROF_WORK_LOCATION" VARCHAR2(32) ,
  "PROF_BUILDING_IDENTIFIER" VARCHAR2(16) ,
  "PROF_DEPARTMENT_NUMBER" VARCHAR2(16) ,
  "PROF_EMPLOYEE_TYPE" VARCHAR2(256) ,
  "PROF_FLOOR" VARCHAR2(16) ,
  "PROF_EMPLOYEE_NUMBER" VARCHAR2(16) ,
  "PROF_PAGER_TYPE" VARCHAR2(16) ,
  "PROF_PAGER_ID" VARCHAR2(32) ,
  "PROF_PAGER_SERVICE_PROVIDER" VARCHAR2(50) ,
  "PROF_PHYSICAL_DELIVERY_OFFICE" VARCHAR2(32) ,
  "PROF_PREFERRED_LANGUAGE" VARCHAR2(100) ,
  "PROF_SHIFT" VARCHAR2(4) ,
  "PROF_TITLE" VARCHAR2(256) ,
  "PROF_COURTESY_TITLE" VARCHAR2(64) ,
  "PROF_TIMEZONE" VARCHAR2(50) , 
  "PROF_NATIVE_LAST_NAME" VARCHAR2(256) ,
  "PROF_NATIVE_FIRST_NAME" VARCHAR2(256) ,
  "PROF_BLOG_URL" VARCHAR2(256) ,
  "PROF_FREEBUSY_URL" VARCHAR2(256) ,
  "PROF_CALENDAR_URL" VARCHAR2(256) ,
  "PROF_DESCRIPTION" CLOB ,
  "PROF_EXPERIENCE" CLOB ,
  "PROF_SOURCE_URL" VARCHAR2(256),
  CONSTRAINT "EMPLOYEE_PK" PRIMARY KEY ("PROF_KEY"))
  TABLESPACE PROFREGTABSPACE ;


-- TO COMPLETE SCHEMA 12 FIXUP, RUN MigrateEmployeeTable JAVA PROGRAM AND THEN FIXUP12B.SQL

COMMIT;

QUIT;

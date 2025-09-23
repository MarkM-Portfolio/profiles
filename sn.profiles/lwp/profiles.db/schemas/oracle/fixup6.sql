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
-- DDL Statements for table "EMPINST"."PROF_CONSTANTS"
------------------------------------------------

CREATE TABLE EMPINST.PROF_CONSTANTS (
    PROF_PROPERTY_KEY VARCHAR (64) NOT NULL,
    PROF_PROPERTY_VALUE VARCHAR (128) NOT NULL,
 	PRIMARY KEY (PROF_PROPERTY_KEY)
) ORGANIZATION INDEX TABLESPACE PROFREGTABSPACE;

INSERT INTO EMPINST.PROF_CONSTANTS VALUES ('USERID_PROPERTY', 'guid');

------------------------------------------------
-- DDL Statements for table "EMPINST"."SNCORE_SCHEMA"
------------------------------------------------

CREATE TABLE EMPINST.SNCORE_SCHEMA  (
	COMPKEY VARCHAR(128) NOT NULL,
	DBSCHEMAVER INTEGER NOT NULL,
	PRIMARY KEY (COMPKEY)
) ORGANIZATION INDEX TABLESPACE PROFREGTABSPACE;

INSERT INTO EMPINST.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_APPEXT_CORE', 1);

------------------------------------------------
-- DDL Statements for view "EMPINST"."SNCORE_PERSON"
------------------------------------------------

CREATE VIEW EMPINST.EMPLOYEE_SNCORE (PROF_USER_ID_TYPE, PROF_KEY, PROF_UID, PROF_GUID, PROF_MAIL_LOWER, PROF_DISPLAY_NAME)
   AS SELECT C.PROF_PROPERTY_VALUE, E.PROF_KEY, E.PROF_UID, E.PROF_GUID, E.PROF_MAIL_LOWER, E.PROF_DISPLAY_NAME  
	   FROM EMPINST.EMPLOYEE E, EMPINST.PROF_CONSTANTS C
 	   WHERE C.PROF_PROPERTY_KEY = 'USERID_PROPERTY';

CREATE VIEW EMPINST.SNCORE_PERSON (SNC_INTERNAL_ID, SNC_IDKEY, SNC_EMAIL_LOWER, SNC_DISPLAY_NAME)
  	AS SELECT E.PROF_KEY, E.PROF_USERID, E.PROF_MAIL_LOWER, E.PROF_DISPLAY_NAME FROM 
	  ((SELECT PROF_KEY, PROF_UID as PROF_USERID, PROF_MAIL_LOWER, PROF_DISPLAY_NAME
	 	  FROM EMPINST.EMPLOYEE_SNCORE WHERE PROF_USER_ID_TYPE = 'uid')
	   UNION
	   (SELECT PROF_KEY, PROF_GUID as PROF_USERID, PROF_MAIL_LOWER, PROF_DISPLAY_NAME
	 	  FROM EMPINST.EMPLOYEE_SNCORE WHERE PROF_USER_ID_TYPE = 'guid')) E;

------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 6 WHERE COMPKEY='Profiles';


COMMIT;

QUIT;

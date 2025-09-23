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
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE EMPINST.EMPLOYEE MODIFY PROF_BUILDING_IDENTIFIER VARCHAR2(64);
	
CREATE INDEX "EMPINST"."PROF_TYPE_IDX" ON "EMPINST"."EMPLOYEE"
        ("PROF_TYPE" ASC) TABLESPACE PROFINDEXTABSPACE;

DROP INDEX EMPINST."EMP_GUID_IDX";

CREATE UNIQUE INDEX EMPINST."EMP_GUID_UDX" ON EMPINST."EMPLOYEE" 
		("PROF_GUID" ASC) TABLESPACE PROFINDEXTABSPACE;

DROP INDEX EMPINST."UID_LOWER_IDX";

CREATE UNIQUE INDEX EMPINST."UID_LOWER_UDX" ON EMPINST."EMPLOYEE" 
		("PROF_UID_LOWER" ASC) TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

ALTER TABLE "EMPINST"."GIVEN_NAME" RENAME TO "GIVEN_NAME_T";

CREATE TABLE EMPINST."GIVEN_NAME"  (
  "PROF_KEY" VARCHAR2(36) NOT NULL , 
  "PROF_GIVENNAME" VARCHAR2(128) NOT NULL ,
  "PROF_NAME_SOURCE" NUMBER(19,0) DEFAULT 0 )
  TABLESPACE PROFREGTABSPACE;


-- copy old data without duplicates 
INSERT INTO EMPINST.GIVEN_NAME (PROF_KEY, PROF_GIVENNAME, PROF_NAME_SOURCE)
(SELECT DISTINCT PROF_KEY, PROF_GIVENNAME, PROF_NAME_SOURCE FROM EMPINST.GIVEN_NAME_T);


DROP TABLE "EMPINST"."GIVEN_NAME_T";
COMMIT;

-- DDL Statements for indexes on Table "EMPINST"."GIVEN_NAME"

CREATE INDEX EMPINST."GIVEN_NAMEX" ON EMPINST."GIVEN_NAME" 
		("PROF_KEY" ASC) TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX EMPINST."GIVEN_NAME_IDX" ON EMPINST."GIVEN_NAME" 
		("PROF_GIVENNAME" ASC) TABLESPACE PROFINDEXTABSPACE;
		
CREATE UNIQUE INDEX EMPINST."GIVEN_NAME_UDX" ON EMPINST."GIVEN_NAME" 
		("PROF_KEY" ASC, "PROF_GIVENNAME" ASC ) TABLESPACE PROFINDEXTABSPACE;



------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------ 


ALTER TABLE "EMPINST"."SURNAME" RENAME TO "SURNAME_T";


CREATE TABLE EMPINST."SURNAME"  (
  "PROF_KEY" VARCHAR2(36) NOT NULL , 
  "PROF_SURNAME" VARCHAR2(128) NOT NULL ,
  "PROF_NAME_SOURCE" NUMBER(19,0) DEFAULT 0 )
  TABLESPACE PROFREGTABSPACE;

-- copy old data without duplicates 
INSERT INTO EMPINST.SURNAME (PROF_KEY, PROF_SURNAME, PROF_NAME_SOURCE)
(SELECT DISTINCT PROF_KEY, PROF_SURNAME, PROF_NAME_SOURCE FROM EMPINST.SURNAME_T);


DROP TABLE "EMPINST"."SURNAME_T";
COMMIT;

-- DDL Statements for indexes on Table "EMPINST"."SURNAME"

CREATE INDEX EMPINST."SURNAMEX" ON EMPINST."SURNAME" 
		("PROF_KEY" ASC) TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX EMPINST."SURNAME_IDX" ON EMPINST."SURNAME" 
		("PROF_SURNAME" ASC) TABLESPACE PROFINDEXTABSPACE;

CREATE UNIQUE INDEX EMPINST."SURNAME_UDX" ON EMPINST."SURNAME" 
		( "PROF_KEY" ASC, "PROF_SURNAME" ASC) TABLESPACE PROFINDEXTABSPACE;


------------------------------------------------
-- DDL Statements for view "EMPINST"."EVENTLOG"
------------------------------------------------

DROP INDEX "EMPINST"."EVLOG_TYPE_IDX";

CREATE INDEX "EMPINST"."EVLOG_TYPE_IDX" ON EMPINST.EVENTLOG ("EVENT_TYPE" ASC, "CREATED" ASC,  "EVENT_KEY" ASC) 
	TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- Update schema version
------------------------------------------------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 16 WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

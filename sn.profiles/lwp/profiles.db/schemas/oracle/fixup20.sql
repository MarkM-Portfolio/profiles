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
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE "EMPINST"."EMPLOYEE" MODIFY "PROF_DEPARTMENT_NUMBER" VARCHAR2(24);

------------------------------------------------
-- DDL Statements for table "EMPINST"."DEPARTMENT"
------------------------------------------------

ALTER TABLE "EMPINST"."DEPARTMENT" MODIFY "PROF_DEPARTMENT_CODE" VARCHAR2(24);
		 
------------------------------------------------
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS_INDEX"
------------------------------------------------

CREATE TABLE "EMPINST"."USER_PLATFORM_EVENTS_INDEX"  (
	"EVENT_KEY_INDEX" NUMBER(19, 0) DEFAULT 0 NOT NULL
) TABLESPACE PROFREGTABSPACE;


-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 20 WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

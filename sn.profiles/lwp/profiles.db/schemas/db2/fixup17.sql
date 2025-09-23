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

------------------------------------------------
-- DDL Statements for view "EMPINST"."EMPLOYEE"
------------------------------------------------


DROP TRIGGER "EMPINST"."T_EMPLOYEE_INSRT"@

DROP TRIGGER "EMPINST"."T_EMPLOYEE_UPDT"@
COMMIT@

 		 
------------------------------------------------
-- DDL Statements for table "EMPINST"."PROFILE_EXTENSIONS"
------------------------------------------------

CREATE TRIGGER "EMPINST"."T_EMPLOYEE_INSRT" NO CASCADE BEFORE INSERT ON "EMPINST"."EMPLOYEE" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_LAST_UPDATE = CURRENT TIMESTAMP@

CREATE TRIGGER "EMPINST"."T_EMPLOYEE_UPDT" NO CASCADE BEFORE UPDATE ON "EMPINST"."EMPLOYEE" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_LAST_UPDATE = CURRENT TIMESTAMP@
COMMIT@

CREATE INDEX EMPINST.PROFILE_EXTENSIONS_IDX2 ON EMPINST.PROFILE_EXTENSIONS
 (PROF_NAME ASC, PROF_PROPERTY_ID ASC) ALLOW REVERSE SCANS@



-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 17 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

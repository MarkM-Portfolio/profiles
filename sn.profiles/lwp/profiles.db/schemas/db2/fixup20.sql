-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2013                                    
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

ALTER TABLE "EMPINST"."EMPLOYEE" ALTER COLUMN "PROF_DEPARTMENT_NUMBER" SET DATA TYPE VARCHAR (24)@

COMMIT@

------------------------------------------------
-- DDL Statements for table "EMPINST"."DEPARTMENT"
------------------------------------------------

ALTER TABLE "EMPINST"."DEPARTMENT" ALTER COLUMN "PROF_DEPARTMENT_CODE" SET DATA TYPE VARCHAR (24)@

COMMIT@

		 
------------------------------------------------
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS_INDEX"
------------------------------------------------

CREATE TABLE "EMPINST"."USER_PLATFORM_EVENTS_INDEX"  (
	"EVENT_KEY_INDEX" BIGINT NOT NULL DEFAULT 0
) IN USERSPACE4K INDEX IN USERSPACE4K@


-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 20 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

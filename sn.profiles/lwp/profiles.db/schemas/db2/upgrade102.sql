-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2007, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

SET INTEGRITY FOR "EMPINST"."EMPLOYEE" OFF@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_UID_LOWER" 
	GENERATED ALWAYS AS (LOWER(PROF_UID))@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_LOGIN" VARCHAR(36)@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_LOGIN_LOWER" 
	GENERATED ALWAYS AS (LOWER(PROF_LOGIN))@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_GW_EMAIL_LOWER" 
	GENERATED ALWAYS AS (LOWER(PROF_GROUPWARE_EMAIL))@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_GIVEN_NAME" VARCHAR(128)@

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_SURNAME" VARCHAR(128) @

ALTER TABLE "EMPINST"."EMPLOYEE" ALTER COLUMN "PROF_DESCRIPTION" SET DATA TYPE VARCHAR (4000)@

ALTER TABLE "EMPINST"."EMPLOYEE" ALTER COLUMN "PROF_EXPERIENCE" SET DATA TYPE VARCHAR (4000)@


SET INTEGRITY FOR EMPINST.EMPLOYEE IMMEDIATE CHECKED FORCE GENERATED@

CREATE INDEX "EMPINST "."UID_LOWER_IDX" ON "EMPINST "."EMPLOYEE" 
		("PROF_UID_LOWER" ASC) ALLOW REVERSE SCANS@

CREATE INDEX "EMPINST "."GW_EMAIL_LOWER_IDX" ON "EMPINST "."EMPLOYEE" 
		("PROF_GW_EMAIL_LOWER" ASC) ALLOW REVERSE SCANS@

CREATE INDEX "EMPINST "."LOGIN_LOWER_IDX" ON "EMPINST "."EMPLOYEE"
                ("PROF_LOGIN_LOWER" ASC) ALLOW REVERSE SCANS@
                         

COMMIT@
CONNECT RESET@

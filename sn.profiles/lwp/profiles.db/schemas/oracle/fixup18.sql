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
-- DDL Statements for table "EMPINST"."PROFILE_EXTENSIONS"
------------------------------------------------

CREATE INDEX "EMPINST"."DISP_IDX" ON "EMPINST"."EMPLOYEE" 
	("PROF_KEY" ASC, "PROF_DISPLAY_NAME" ASC)
	TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX "EMPINST"."PROF_SNGN_IDX" ON "EMPINST"."EMPLOYEE" 
	("PROF_KEY" ASC, "PROF_SURNAME" ASC, "PROF_GIVEN_NAME" ASC)
	TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX "EMPINST"."PROF_GN2_IDX" ON "EMPINST"."EMPLOYEE" 
	("PROF_GIVEN_NAME" ASC, "PROF_KEY" ASC)
	TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX "EMPINST"."PROF_SN2_IDX" ON "EMPINST"."EMPLOYEE" 
	("PROF_SURNAME" ASC, "PROF_KEY" ASC)
	TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- DDL Statements for view "EMPINST"."PRONUNCIATION"
------------------------------------------------

ALTER TABLE "EMPINST"."PRONUNCIATION" ADD "PROF_FILE_TYPE" VARCHAR (50) DEFAULT 'audio/wav' NOT NULL ;

------------------------------------------------
-- Update schema version
------------------------------------------------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 18, RELEASEVER='2.5.1.0' WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

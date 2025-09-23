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
-- DDL Statements for table "EMPINST"."PROFILE_EXTENSIONS"
------------------------------------------------

CREATE INDEX "EMPINST"."PROFILE_EXTENSIONS_IDX2" ON "EMPINST"."PROFILE_EXTENSIONS" 
	("PROF_NAME" ASC, "PROF_PROPERTY_ID" ASC)
	TABLESPACE PROFINDEXTABSPACE;

------------------------------------------------
-- Update schema version
------------------------------------------------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 17 WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

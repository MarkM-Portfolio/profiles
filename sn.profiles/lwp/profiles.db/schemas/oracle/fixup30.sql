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

{include.msgVector-fixup6.sql}

COMMIT;

-- Update schema versions

DROP INDEX "EMPINST"."EVLOG_AUDIT_IDX";
COMMIT;

CREATE INDEX "EMPINST"."EVLOG_AUDIT_IDX" ON EMPINST.EVENTLOG ("ISSYSEVENT" ASC, "EVENT_KEY" ASC) 
	TABLESPACE PROFINDEXTABSPACE;
COMMIT;
	
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 30, RELEASEVER='3.0.1.0' WHERE COMPKEY='Profiles';

COMMIT;
QUIT;

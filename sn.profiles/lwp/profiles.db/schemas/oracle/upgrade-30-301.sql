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

--
-- Board schema updates
--
{include.msgVector-fixup6.sql}
COMMIT;

-- Conditionally create index for customers where index was not created correctly
DECLARE
    i INTEGER;
BEGIN
    SELECT COUNT(*) INTO i FROM all_indexes WHERE index_name = 'EVLOG_AUDIT_IDX';
    IF i = 0 THEN
        EXECUTE IMMEDIATE 'CREATE INDEX EVLOG_AUDIT_IDX ON EMPINST.EVENTLOG (ISSYSEVENT ASC, EVENT_KEY ASC) TABLESPACE PROFINDEXTABSPACE';
    END IF;
END;
/

COMMIT;

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 30, RELEASEVER='3.0.1.0' WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

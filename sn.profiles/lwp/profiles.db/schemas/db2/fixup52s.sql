-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2015
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

CONNECT TO PEOPLEDB@

------
-- add home org/tenant key 
------

ALTER TABLE EMPINST.EMPLOYEE ADD COLUMN H_TENANT_KEY VARCHAR(36)@
COMMIT@

------
-- stored prcedure to back fill H_TENANT_KEY
------

CREATE OR REPLACE PROCEDURE EMPINST.BACKFILL_HOME_TENANT ( 
                                                        OUT row_cnt   INTEGER,
                                                        OUT begin_ts  TIMESTAMP,                                                      
                                                        OUT end_ts    TIMESTAMP,
                                                        OUT last_key  VARCHAR(36) ) 
LANGUAGE SQL MODIFIES SQL DATA 
BEGIN 
DECLARE SQLCODE INTEGER DEFAULT 0 ;
DECLARE my_counter_batch INTEGER ;
DECLARE my_key VARCHAR(36) ;
DECLARE my_update_sql VARCHAR(512);
DECLARE at_end SMALLINT DEFAULT 0 ;
DECLARE my_update STATEMENT;
DECLARE c CURSOR WITH HOLD FOR SELECT PROF_KEY FROM EMPINST.EMPLOYEE WHERE H_TENANT_KEY IS NULL FETCH FIRST 10 ROWS ONLY OPTIMIZE FOR 10 ROWS ;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET at_end = 1 ;

SET begin_ts = CURRENT TIMESTAMP ;
SET my_update_sql = 'UPDATE EMPINST.EMPLOYEE E SET E.H_TENANT_KEY = E.TENANT_KEY WHERE E.PROF_KEY = ?';
SET row_cnt = 0 ;
PREPARE my_update FROM my_update_sql ;
NEXTBATCH: LOOP 
set at_end = 0 ;
OPEN c ;
SET my_counter_batch = 0;
FETCH FROM c INTO my_key ;
IF at_end = 1 THEN LEAVE NEXTBATCH; END IF;
set last_key = my_key ;
NEXTROW: LOOP
          EXECUTE my_update USING my_key ;
          SET my_counter_batch = my_counter_batch + 1; 
          FETCH FROM c INTO my_key ;
          IF at_end = 1 THEN LEAVE NEXTROW; END IF;
          set last_key = my_key ;
END LOOP NEXTROW ; 
SET row_cnt = row_cnt + my_counter_batch;

CLOSE c;
COMMIT;
IF my_counter_batch < 10 THEN LEAVE NEXTBATCH; END IF;
END LOOP NEXTBATCH ; 

SET end_ts = CURRENT TIMESTAMP ;
END @

COMMIT@
------
-- reorg table after adding column
------

REORG TABLE EMPINST.EMPLOYEE@
COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=52, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------

CONNECT RESET@

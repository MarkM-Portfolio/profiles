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
-- back fill H_TENANT_KEY and set column to not null
------

CALL  EMPINST.BACKFILL_HOME_TENANT( ?, ? , ?, ?)@

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN H_TENANT_KEY SET NOT NULL@
COMMIT@

------
-- reorg table after adding column
------

REORG TABLE EMPINST.EMPLOYEE@
COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=53, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------

CONNECT RESET@

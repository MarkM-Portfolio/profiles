-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2014
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

CONNECT TO PEOPLEDB@

------
-- add EMPLOYEE.PROF_IDHASH
------

ALTER TABLE EMPINST.EMPLOYEE ADD COLUMN PROF_IDHASH VARCHAR(256) DEFAULT '?' NOT NULL@

CREATE INDEX EMPINST.IDHASH_UDX ON EMPINST.EMPLOYEE (PROF_IDHASH ASC, TENANT_KEY) ALLOW REVERSE SCANS@

COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 48, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------

CONNECT RESET@

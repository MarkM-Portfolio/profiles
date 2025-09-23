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

SET CURRENT SCHEMA EMPINST;

------
-- add EMPLOYEE.PROF_IDHASH
------

CREATE UNIQUE INDEX EMPINST.TENANT_EXID_UDX ON EMPINST.TENANT (TENANT_EXID ASC) ALLOW REVERSE SCANS ;

COMMIT;

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 49, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';

COMMIT;

------
-- Disconnect
------

--CONNECT RESET;

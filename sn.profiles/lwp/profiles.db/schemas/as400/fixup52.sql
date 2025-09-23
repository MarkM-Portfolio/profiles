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

SET CURRENT SCHEMA EMPINST;

------
-- add home org/tenant key 
------

ALTER TABLE EMPINST.EMPLOYEE ADD COLUMN H_TENANT_KEY VARCHAR(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL;
COMMIT;

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 52, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

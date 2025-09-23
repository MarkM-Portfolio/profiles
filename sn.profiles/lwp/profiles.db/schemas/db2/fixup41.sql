-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2013                                   
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

--====
-- Support a type on a tag
--====

ALTER TABLE EMPINST.PEOPLE_TAG
	ADD COLUMN PROF_TYPE VARCHAR(36) NOT NULL DEFAULT 'general'@

CREATE INDEX EMPINST.PEOPLE_TAG_IDX3 ON EMPINST.PEOPLE_TAG 
		(PROF_TAG ASC, PROF_TYPE ASC, TENANT_KEY) @
		
DROP INDEX EMPINST.PEOPLE_TAG_UDX@

REORG TABLE EMPINST.PEOPLE_TAG@

CREATE UNIQUE INDEX EMPINST.PEOPLE_TAG_UDX ON EMPINST.PEOPLE_TAG 
	(PROF_SOURCE_KEY ASC, PROF_TARGET_KEY ASC, PROF_TAG ASC, PROF_TYPE ASC, TENANT_KEY)@

COMMIT@

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 41, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------
CONNECT RESET@



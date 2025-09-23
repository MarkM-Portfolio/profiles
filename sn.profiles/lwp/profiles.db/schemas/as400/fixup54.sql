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

------------------------------------------------
-- DDL Statements for table EMPINST.EMP_UPDATE_TIMESTAMP
------------------------------------------------

CREATE TABLE EMPINST.EMP_UPDATE_TIMESTAMP (
	PROF_KEY 						VARCHAR(36) CCSID 1208 NOT NULL, 
	TENANT_KEY						VARCHAR(36) CCSID 1208 DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL,
	LDAP_MOD_TIMESTAMP_SOURCE		VARCHAR(36)  CCSID 1208 NOT NULL,
	LDAP_MOD_TIMESTAMP	 			VARCHAR(128) CCSID 1208 DEFAULT '0' NOT NULL,
	MARK 							VARCHAR(1)  CCSID 1208 DEFAULT '0' NOT NULL
) ;

ALTER TABLE EMPINST.EMP_UPDATE_TIMESTAMP ADD CONSTRAINT EMPINST.TS_PK PRIMARY KEY (PROF_KEY, TENANT_KEY, LDAP_MOD_TIMESTAMP_SOURCE);

COMMIT:

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 54, RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

--CONNECT RESET;

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

--====
-- EMPLOYEE ROLE MAPPING
--====

CREATE TABLE EMPINST.EMP_ROLE_MAP (
	MAP_KEY        VARCHAR2(36)  NOT NULL,
	PROF_KEY       VARCHAR2(36)  NOT NULL,
	ROLE_ID        VARCHAR2(128) NOT NULL,
	CREATED        TIMESTAMP     NOT NULL,
	TENANT_KEY     VARCHAR2(36)  DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
)
	TABLESPACE PROFREGTABSPACE ;
COMMIT;
 
INSERT INTO EMPINST.EMP_ROLE_MAP(
  MAP_KEY, PROF_KEY, ROLE_ID, CREATED, TENANT_KEY)
SELECT E.PROF_KEY AS MAP_KEY, E.PROF_KEY AS PROF_KEY, CAST('employee' AS VARCHAR2(128)) AS ROLE_ID, CAST(LOCALTIMESTAMP AS TIMESTAMP) AS CREATED, E.TENANT_KEY
   FROM EMPINST.EMPLOYEE E;
COMMIT;

ALTER TABLE EMPINST.EMP_ROLE_MAP ADD CONSTRAINT EMP_ROLEMAP_PK PRIMARY KEY (MAP_KEY, TENANT_KEY) USING INDEX TABLESPACE PROFINDEXTABSPACE;

CREATE UNIQUE INDEX EMPINST.EMP_ROLE_UDX ON EMPINST.EMP_ROLE_MAP (PROF_KEY, ROLE_ID, TENANT_KEY) TABLESPACE PROFINDEXTABSPACE;
COMMIT ;

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 45, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

QUIT;
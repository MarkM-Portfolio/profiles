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

--====
-- EMPLOYEE ROLE MAPPING
--====

CREATE TABLE EMPINST.EMP_ROLE_MAP(
	MAP_KEY        VARCHAR(36) CCSID 1208 NOT NULL ,
	PROF_KEY       VARCHAR(36) CCSID 1208 NOT NULL ,
	ROLE_ID        VARCHAR(128) CCSID 1208 NOT NULL ,
	CREATED        TIMESTAMP NOT NULL ,
	TENANT_KEY	   VARCHAR(36) CCSID 1208 DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
) ;

ALTER TABLE EMPINST.EMP_ROLE_MAP ADD CONSTRAINT EMPINST.EMP_ROLEMAP_PK PRIMARY KEY (MAP_KEY, TENANT_KEY);

CREATE UNIQUE INDEX EMPINST.EMP_ROLE_UDX ON EMPINST.EMP_ROLE_MAP (PROF_KEY, ROLE_ID, TENANT_KEY);

------
-- export and import role info
------
/*
EXPORT TO emproles.exp.ixf OF IXF 
    METHOD N ( MAP_KEY, PROF_KEY, ROLE_ID, CREATED, TENANT_KEY) 
    MESSAGES emproles.exp.msg 
    SELECT E.PROF_KEY AS MAP_KEY,
           E.PROF_KEY AS PROF_KEY,
           'employee' AS ROLE_ID,
           CURRENT_TIMESTAMP AS CREATED,
           E.TENANT_KEY AS TENANT_KEY
    FROM EMPINST.EMPLOYEE E ;

COMMIT;

IMPORT FROM emproles.exp.ixf OF IXF 
    METHOD N ( MAP_KEY, PROF_KEY, ROLE_ID, CREATED, TENANT_KEY ) 
    COMMITCOUNT 1000 
    MESSAGES emproles.imp.msg  
    INSERT_UPDATE INTO EMPINST.EMP_ROLE_MAP ( MAP_KEY, PROF_KEY, ROLE_ID, CREATED, TENANT_KEY  ) ;

*/	
INSERT INTO EMPINST.EMP_ROLE_MAP ( MAP_KEY, PROF_KEY, ROLE_ID, CREATED, TENANT_KEY ) SELECT E.PROF_KEY, E.PROF_KEY,'employee' ,CURRENT_TIMESTAMP, E.TENANT_KEY FROM EMPINST.EMPLOYEE E;

COMMIT ;

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 45, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
COMMIT;

------
-- Disconnect
------

--CONNECT RESET;

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
USE PEOPLEDB;
GO

--====
-- EMPLOYEE ROLE MAPPING
--====

CREATE TABLE EMPINST.EMP_ROLE_MAP (
	MAP_KEY        NVARCHAR(36)  NOT NULL,
	PROF_KEY       NVARCHAR(36)  NOT NULL,
	ROLE_ID        NVARCHAR(128) NOT NULL,
	CREATED        DATETIME      NOT NULL,
	TENANT_KEY     NVARCHAR(36)  DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL
);
GO

ALTER TABLE EMPINST.EMP_ROLE_MAP ADD CONSTRAINT EMP_ROLEMAP_PK PRIMARY KEY (MAP_KEY, TENANT_KEY);
GO

CREATE UNIQUE INDEX EMP_ROLE_UDX ON EMPINST.EMP_ROLE_MAP (PROF_KEY, ROLE_ID, TENANT_KEY);
GO

------
-- create role data
------

INSERT INTO EMPINST.EMP_ROLE_MAP
SELECT E.PROF_KEY AS MAP_KEY,
       E.PROF_KEY AS PROF_KEY,
       CAST(N'employee' AS NVARCHAR(128)) AS ROLE_ID,
       CURRENT_TIMESTAMP AS CREATED,
       E.TENANT_KEY AS TENANT_KEY
FROM EMPINST.EMPLOYEE E;

--====
-- Update schema version
--====

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 45, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
GO

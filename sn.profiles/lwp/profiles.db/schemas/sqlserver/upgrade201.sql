-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2007, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_LOGIN NVARCHAR(256);
ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_DESCRIPTION NVARCHAR (max);
ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_EXPERIENCE  NVARCHAR (max);
GO

CREATE INDEX SRC_UID_IDX ON EMPINST.EMPLOYEE 
		(PROF_SOURCE_UID ASC);
GO

ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_LOGIN NVARCHAR(256);
ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_DESCRIPTION NVARCHAR (max);
ALTER TABLE EMPINST.EMP_DRAFT ALTER COLUMN PROF_EXPERIENCE  NVARCHAR (max);
GO
                    
DROP INDEX EMPINST.PROF_CONNECTIONS.CONN_UDX;
DROP INDEX EMPINST.PROF_CONNECTIONS.CONN_INDEX1;
DROP INDEX EMPINST.PROF_CONNECTIONS.CONN_INDEX2; 
GO

ALTER TABLE EMPINST.PROF_CONNECTIONS DROP COLUMN PROF_VISIBILITY;
ALTER TABLE EMPINST.PROF_CONNECTIONS ALTER COLUMN PROF_TYPE NVARCHAR(36);
GO

UPDATE EMPINST.PROF_CONNECTIONS SET PROF_TYPE=PROF_VALUE;
ALTER TABLE EMPINST.PROF_CONNECTIONS DROP COLUMN PROF_VALUE;
GO

CREATE UNIQUE INDEX CONN_UDX ON EMPINST.PROF_CONNECTIONS
		(PROF_SOURCE_KEY, PROF_TARGET_KEY, PROF_TYPE);

-- Main index to select a users connections
CREATE INDEX CONN_INDEX1 ON EMPINST.PROF_CONNECTIONS
	(PROF_STATUS, PROF_TYPE,  PROF_SOURCE_KEY);

CREATE INDEX CONN_INDEX2 ON EMPINST.PROF_CONNECTIONS
	(PROF_STATUS, PROF_TYPE,  PROF_SOURCE_KEY, PROF_TARGET_KEY); 
GO


------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 5 WHERE COMPKEY='Profiles';
GO

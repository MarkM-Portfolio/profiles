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
-- NAME TABLES: Indexing to incorporate PROF_MODE
--====

DROP INDEX GIVEN_NAME_IDX ON EMPINST.GIVEN_NAME;
CREATE INDEX GIVEN_NAME_IDX ON EMPINST.GIVEN_NAME (PROF_GIVENNAME ASC, PROF_KEY ASC, PROF_USRSTATE ASC, PROF_MODE ASC, TENANT_KEY);
GO

DROP INDEX GIVEN_NAME_IDX2 ON EMPINST.GIVEN_NAME;
CREATE INDEX GIVEN_NAME_IDX2 ON EMPINST.GIVEN_NAME (PROF_USRSTATE ASC, PROF_MODE ASC, PROF_GIVENNAME ASC, PROF_KEY ASC, TENANT_KEY);
GO

DROP INDEX GIVEN_NAMEX ON EMPINST.GIVEN_NAME;
CREATE INDEX GIVEN_NAMEX ON EMPINST.GIVEN_NAME (PROF_KEY ASC, PROF_GIVENNAME ASC, PROF_USRSTATE ASC, PROF_MODE ASC, TENANT_KEY);
GO

DROP INDEX SURNAME_IDX ON EMPINST.SURNAME;
CREATE INDEX SURNAME_IDX ON EMPINST.SURNAME (PROF_SURNAME ASC, PROF_KEY ASC, PROF_USRSTATE ASC, PROF_MODE ASC, TENANT_KEY);
GO

DROP INDEX SURNAME_IDX2 ON EMPINST.SURNAME;
CREATE INDEX SURNAME_IDX2 ON EMPINST.SURNAME (PROF_USRSTATE ASC, PROF_MODE ASC, PROF_SURNAME ASC, PROF_KEY ASC, TENANT_KEY);
GO

DROP INDEX SURNAMEX ON EMPINST.SURNAME;
CREATE INDEX SURNAMEX ON EMPINST.SURNAME (PROF_KEY ASC, PROF_SURNAME ASC, PROF_USRSTATE ASC, PROF_MODE ASC, TENANT_KEY);
GO

--====
-- EMPLOYEE TABLE: Index to incorporate PROF_MODE
--====

CREATE INDEX PROF_MODE ON EMPINST.EMPLOYEE (PROF_MODE ASC, TENANT_KEY);
GO

------
-- Update schema versions
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 44, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2013, 2014                             
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
-- cleanup DYNA ATTR content
--====
------
-- DYNA_OBJ_REF
------
ALTER TABLE EMPINST.DYNA_ATTRS DROP CONSTRAINT DYNA_ATTR_FK;
GO

ALTER TABLE EMPINST.DYNA_OBJ_REF DROP CONSTRAINT DYNA_DEF_FK;
GO

DROP INDEX DYNA_RESASSOC_DEFLK ON EMPINST.DYNA_OBJ_REF;
GO

DROP INDEX DYNA_RESASSOC_BIDX ON EMPINST.DYNA_OBJ_REF;
GO

DROP INDEX DYNA_RESASSOC_SIDX ON EMPINST.DYNA_OBJ_REF;
GO

DROP TABLE  EMPINST.DYNA_OBJ_REF;
GO

------
-- DYNA_DEFS
------
DROP INDEX DYNA_DEFS_APPIDX ON EMPINST.DYNA_DEFS;
GO

DROP INDEX DYNA_DEFS_LK ON EMPINST.DYNA_DEFS;
GO

DROP TABLE EMPINST.DYNA_DEFS;
GO

DROP TABLE EMPINST.DYNA_ATTRS;
GO

------
-- SNCORE_SCHEMA
------
DROP TABLE EMPINST.SNCORE_SCHEMA;
GO

DELETE FROM EMPINST.PROF_CONSTANTS WHERE PROF_PROPERTY_KEY = 'LA_IDX_STATE';
DELETE FROM EMPINST.PROF_CONSTANTS WHERE PROF_PROPERTY_KEY = 'sys.usrPhotoThumbnail.task';
INSERT INTO EMPINST.PROF_CONSTANTS VALUES ('sys.usrPhotoThumbnail.task', 'complete');
GO

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 38, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO


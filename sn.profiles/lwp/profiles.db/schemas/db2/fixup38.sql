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
CONNECT TO PEOPLEDB@

--====
-- cleanup DYNA ATTR content
--====
------
-- DYNA_OBJ_REF
------
ALTER TABLE EMPINST.DYNA_OBJ_REF DROP CONSTRAINT DYNA_DEF_FK@
COMMIT@

DROP INDEX EMPINST.DYNA_RESASSOC_DEFLK@
COMMIT@

DROP INDEX EMPINST.DYNA_RESASSOC_BIDX@
COMMIT@

DROP INDEX EMPINST.DYNA_RESASSOC_SIDX@
COMMIT@

DROP TABLE  EMPINST.DYNA_OBJ_REF@
COMMIT@

------
-- DYNA_DEFS
------
DROP INDEX EMPINST.DYNA_DEFS_APPIDX@
COMMIT@

DROP INDEX EMPINST.DYNA_DEFS_LK@
COMMIT@

DROP TABLE EMPINST.DYNA_DEFS@
COMMIT@

DROP TABLE EMPINST.DYNA_ATTRS@
COMMIT@

------
-- SNCORE_SCHEMA
------
DROP TABLE EMPINST.SNCORE_SCHEMA@
COMMIT@

DELETE FROM EMPINST.PROF_CONSTANTS WHERE PROF_PROPERTY_KEY = 'LA_IDX_STATE'@
DELETE FROM EMPINST.PROF_CONSTANTS WHERE PROF_PROPERTY_KEY = 'sys.usrPhotoThumbnail.task'@
INSERT INTO EMPINST.PROF_CONSTANTS VALUES ('sys.usrPhotoThumbnail.task', 'complete')@
COMMIT@

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 38, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------
CONNECT RESET@



-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

DROP TABLE EMPINST.USER_STATE_LAIDX@

-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-- START: Dynattr Schema
-- %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

-- ================================================================================
-- Remove constraints for update
-- ================================================================================

-- clear the DB@ tables should be empty at this stage
DELETE FROM EMPINST.DYNA_OBJ_REF@ 
DELETE FROM EMPINST.DYNA_DEFS@

-- remove FK
ALTER TABLE EMPINST.DYNA_OBJ_REF DROP CONSTRAINT DYNA_DEF_FK@

-- drop table for rebuilding
DROP TABLE EMPINST.DYNA_DEFS@

-- ================================================================================
-- DYNA_ATTR Definitions table@ use to constrain index values and object references
-- ================================================================================

CREATE TABLE EMPINST.DYNA_DEFS (
	DEFID		CHAR(16) FOR BIT DATA NOT NULL,
	DEFTYPE		INTEGER NOT NULL,
	APPID		VARCHAR(64) NOT NULL,
	OBJTYPE		VARCHAR(64),
	OBJFACET	VARCHAR(128),
	DEFMD5		CHAR(32) NOT NULL,
	DEFPROPS	CLOB(1M) LOGGED, 
	CONSTRAINT PK PRIMARY KEY (DEFID)
) IN USERSPACE4K INDEX IN USERSPACE4K@

CREATE UNIQUE INDEX EMPINST.DYNA_DEFS_LK ON EMPINST.DYNA_DEFS (
	DEFTYPE ASC,
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
)@

CREATE UNIQUE INDEX EMPINST.DYNA_DEFS_APPIDX ON EMPINST.DYNA_DEFS (
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
)@

-- ================================================================================
-- Add FK back for DYNA_OBJ_REF
-- ================================================================================

ALTER TABLE EMPINST.DYNA_OBJ_REF ADD CONSTRAINT DYNA_DEF_FK FOREIGN KEY (DEFID)
	REFERENCES EMPINST.DYNA_DEFS (DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- User state look aside index table
--
CREATE TABLE EMPINST.USER_STATE_LAIDX (
 	DEFID    CHAR(16) FOR BIT DATA NOT NULL,
 	IDXID    CHAR(16) FOR BIT DATA NOT NULL,
 	USRSTATE   INTEGER NOT NULL,
 	USRSTATE_UPDATED TIMESTAMP NOT NULL,
 	OBJID    CHAR(16) FOR BIT DATA NOT NULL,
 	OBJIDSTR   CHAR(36) NOT NULL,
 	CONSTRAINT PK PRIMARY KEY (IDXID)
) IN USERSPACE4K INDEX IN USERSPACE4K@


--
-- Foriegn key constraint on def table to clean up on deletion
-- 
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_OBJ_FK FOREIGN KEY (OBJID) 
	REFERENCES EMPINST.DYNA_OBJ_REF(OBJID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- Constrain to one value per user
--
CREATE UNIQUE INDEX USRSTATE_CONS_VALB 
	ON EMPINST.USER_STATE_LAIDX (OBJID ASC)
	INCLUDE (OBJIDSTR, USRSTATE, USRSTATE_UPDATED)
	ALLOW REVERSE SCANS@
	
CREATE UNIQUE INDEX USRSTATE_CONS_VALS 
	ON EMPINST.USER_STATE_LAIDX (OBJIDSTR ASC)
	INCLUDE (OBJID, USRSTATE, USRSTATE_UPDATED)
	ALLOW REVERSE SCANS@

--
-- Main index for scanning
--
CREATE INDEX USRSTATE_IDX
	ON EMPINST.USER_STATE_LAIDX (USRSTATE ASC, USRSTATE_UPDATED ASC, OBJID ASC, OBJIDSTR ASC)
	ALLOW REVERSE SCANS@


-- ================================================================================
-- FIX Schema Version
-- ================================================================================
UPDATE EMPINST.SNCORE_SCHEMA SET DBSCHEMAVER = 4 WHERE COMPKEY = 'LC_APPEXT_DYNATTR_DEFS'@

-- Add state field for lookaside index tables
UPDATE EMPINST.PROF_CONSTANTS SET PROF_PROPERTY_VALUE = 'inconsistent' WHERE PROF_PROPERTY_KEY = 'LA_IDX_STATE'@

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 26 WHERE COMPKEY='Profiles'@


COMMIT@
CONNECT RESET@

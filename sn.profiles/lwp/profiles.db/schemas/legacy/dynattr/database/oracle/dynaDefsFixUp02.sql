-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2007, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

-- ================================================================================
-- Remove constraints for update
-- ================================================================================

-- clear the DB; tables should be empty at this stage
DELETE FROM {SUBST_SCHEMA}.DYNA_DEFS; 
-- remove FK
ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF DROP CONSTRAINT DYNA_DEF_FK;
-- drop table for rebuilding
DROP TABLE {SUBST_SCHEMA}.DYNA_DEFS;

-- ================================================================================
-- DYNA_ATTR Definitions table; use to constrain index values and object references
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_DEFS (
	DEFID		RAW(16) NOT NULL,
	DEFTYPE		INTEGER NOT NULL,
	APPID		VARCHAR2(64) NOT NULL,
	OBJTYPE		VARCHAR2(64) NOT NULL,
	OBJFACET	VARCHAR2(128) NOT NULL,
	DEFMD5		CHAR(32) NOT NULL,
	DEFPROPS	CLOB, 
	CONSTRAINT DYNA_DEF_PK PRIMARY KEY (DEFID)
) TABLESPACE {SUBST_ORACLE_TABSPACE4K};

CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_DEFS_LK ON {SUBST_SCHEMA}.DYNA_DEFS (
	DEFTYPE ASC,
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
) TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_DEFS_APPIDX ON {SUBST_SCHEMA}.DYNA_DEFS (
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
) TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

-- ================================================================================
-- Add FK back for DYNA_OBJ_REF
-- ================================================================================

ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF ADD CONSTRAINT DYNA_DEF_FK FOREIGN KEY (DEFID)
	REFERENCES {SUBST_SCHEMA}.DYNA_DEFS (DEFID)
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

-- ================================================================================
-- FIX Schema Version
-- ================================================================================
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER = 2 WHERE COMPKEY = 'LC_APPEXT_DYNATTR_DEFS';

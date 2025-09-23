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
GO
-- remove FK
ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF DROP CONSTRAINT DYNA_DEF_FK;
GO
-- drop table for rebuilding
DROP TABLE {SUBST_SCHEMA}.DYNA_DEFS;
GO

-- ================================================================================
-- DYNA_ATTR Definitions table; use to constrain index values and object references
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_DEFS (
	DEFID		BINARY(16) NOT NULL,
	DEFTYPE		INT NOT NULL,
	APPID		NVARCHAR(64) NOT NULL,
	OBJTYPE		NVARCHAR(64),
	OBJFACET	NVARCHAR(128),
	DEFMD5		NCHAR(32) NOT NULL,
	DEFPROPS	NVARCHAR(MAX), 
	CONSTRAINT DYNA_DEFS_PK PRIMARY KEY (DEFID)
);
GO

CREATE UNIQUE INDEX DYNA_DEFS_LK ON {SUBST_SCHEMA}.DYNA_DEFS (
	DEFTYPE ASC,
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
);
GO

CREATE UNIQUE INDEX DYNA_DEFS_APPIDX ON {SUBST_SCHEMA}.DYNA_DEFS (
	APPID ASC,
	OBJTYPE ASC,
	OBJFACET ASC
);
GO

-- ================================================================================
-- Add FK back for DYNA_OBJ_REF
-- ================================================================================

ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF ADD CONSTRAINT DYNA_DEF_FK FOREIGN KEY (DEFID)
	REFERENCES {SUBST_SCHEMA}.DYNA_DEFS (DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- ================================================================================
-- FIX Schema Version
-- ================================================================================
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER = 4 WHERE COMPKEY = 'LC_APPEXT_DYNATTR_DEFS';
GO

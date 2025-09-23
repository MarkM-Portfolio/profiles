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

-- drop table for rebuilding
DROP TABLE {SUBST_SCHEMA}.DYNA_ATTRS@

-- ================================================================================
-- DYNA_ATTR Definitions table; use to constrain index values and object references
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_ATTRS (
	ATTRID		CHAR(16) FOR BIT DATA NOT NULL,
	OBJID		CHAR(16) FOR BIT DATA NOT NULL,
	ATTRFLAGS	INTEGER	NOT NULL,
	ATTRKEY		VARCHAR(256) NOT NULL,
	ATTRORDER	INTEGER NOT NULL,
	ATTRVAL		VARCHAR(2000),
	ATTRVALEXT	BLOB(1M) LOGGED
) IN {SUBST_USERSPACE4K} INDEX IN {SUBST_USERSPACE4K_INDEX}@

CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_ATTRS_UIDX 
	ON {SUBST_SCHEMA}.DYNA_ATTRS (OBJID ASC, ATTRKEY ASC, ATTRORDER ASC)
	CLUSTER ALLOW REVERSE SCANS@

ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS 
	ADD CONSTRAINT PK PRIMARY KEY(ATTRID)@
	
ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS ADD CONSTRAINT DYNA_ATTR_FK FOREIGN KEY (OBJID)
	REFERENCES {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

-- ================================================================================
-- FIX Schema Version
-- ================================================================================
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER = 3 WHERE COMPKEY = 'LC_APPEXT_DYNATTR_DEFS'@

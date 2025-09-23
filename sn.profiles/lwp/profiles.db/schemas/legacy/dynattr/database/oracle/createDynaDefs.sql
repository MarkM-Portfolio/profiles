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

INSERT INTO {SUBST_SCHEMA}.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_APPEXT_DYNATTR_DEFS', 5);

-- ================================================================================
-- DYNA_ATTR Definitions table; use to constrain index values and object references
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_DEFS (
	DEFID		RAW(16) NOT NULL,
	DEFTYPE		INTEGER NOT NULL,
	APPID		VARCHAR2(64) NOT NULL,
	OBJTYPE		VARCHAR2(64),
	OBJFACET	VARCHAR2(128),
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
-- Object Refererence
--	Used as means to reference data for variety of different data types
--	OBJDESC field is mainly used as human readible field to figure out what
--	 sort of resource is referenced.
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF (
	DEFID		RAW(16) NOT NULL,
	OBJID		RAW(16) NOT NULL,
	OBJIDSTR	CHAR(36) NOT NULL,
	OBJDESC		VARCHAR2(256) NOT NULL,
	CONSTRAINT DYNA_OBJ_PK PRIMARY KEY (OBJID)
) TABLESPACE {SUBST_ORACLE_TABSPACE4K};

-- FK
ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF ADD CONSTRAINT DYNA_DEF_FK FOREIGN KEY (DEFID)
	REFERENCES {SUBST_SCHEMA}.DYNA_DEFS (DEFID)
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

-- INDEX of string representation / binary representation
CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_RESASSOC_SIDX
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJIDSTR ASC, OBJID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

-- INDEX of binary representation / string representation
CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_RESASSOC_BIDX
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJID ASC, OBJIDSTR ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};
	
-- INDEX to lookup resources of same type and handling deletes
CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_RESASSOC_DEFLK
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (DEFID ASC, OBJID ASC, OBJIDSTR ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

	
-- ================================================================================
-- DYNA_ATTR ATTR table; This is the primary data storage object
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_ATTRS (
	ATTRID		RAW(16) NOT NULL,
	OBJID		RAW(16) NOT NULL,
	ATTRFLAGS	INTEGER	NOT NULL,
	ATTRUPDATED	TIMESTAMP NOT NULL,
	ATTRKEY		VARCHAR2(256) NOT NULL,
	ATTRORDER	INTEGER NOT NULL,
	ATTRVAL		VARCHAR2(2000),
	ATTRVALEXT	BLOB
) TABLESPACE {SUBST_ORACLE_TABSPACE4K};

CREATE UNIQUE INDEX {SUBST_SCHEMA}.DYNA_ATTRS_UIDX 
	ON {SUBST_SCHEMA}.DYNA_ATTRS (OBJID ASC, ATTRKEY ASC, ATTRORDER ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS 
	ADD CONSTRAINT ATTR_PK PRIMARY KEY(ATTRID);
	
ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS ADD CONSTRAINT DYNA_ATTR_FK FOREIGN KEY (OBJID)
	REFERENCES {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJID)
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

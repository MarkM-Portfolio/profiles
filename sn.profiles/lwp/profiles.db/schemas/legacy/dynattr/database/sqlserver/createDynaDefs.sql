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
-- Object Refererence
--	Used as means to reference data for variety of different data types
--	OBJDESC field is mainly used as human readible field to figure out what
--	 sort of resource is referenced.
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF (
	DEFID		BINARY(16) NOT NULL,
	OBJID		BINARY(16) NOT NULL,
	OBJIDSTR	NCHAR(36) NOT NULL,
	OBJDESC		NVARCHAR(256) NOT NULL,
	CONSTRAINT DYNA_OBJ_PK PRIMARY KEY (OBJID)
);
GO

-- FK
ALTER TABLE {SUBST_SCHEMA}.DYNA_OBJ_REF ADD CONSTRAINT DYNA_DEF_FK FOREIGN KEY (DEFID)
	REFERENCES {SUBST_SCHEMA}.DYNA_DEFS (DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- INDEX of string representation / binary representation
CREATE UNIQUE INDEX DYNA_RESASSOC_SIDX
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJIDSTR ASC, OBJID ASC);

-- INDEX of binary representation / string representation
CREATE UNIQUE INDEX DYNA_RESASSOC_BIDX
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJID ASC, OBJIDSTR ASC);
	
-- INDEX to lookup resources of same type and handling deletes
CREATE UNIQUE INDEX DYNA_RESASSOC_DEFLK
	ON {SUBST_SCHEMA}.DYNA_OBJ_REF (DEFID ASC, OBJID ASC, OBJIDSTR ASC);
GO
	
-- ================================================================================
-- DYNA_ATTR ATTR table; This is the primary data storage object
-- ================================================================================

CREATE TABLE {SUBST_SCHEMA}.DYNA_ATTRS (
	ATTRID		BINARY(16)NOT NULL,
	OBJID		BINARY(16)NOT NULL,
	ATTRFLAGS	INTEGER	NOT NULL,
	ATTRUPDATED	DATETIME NOT NULL,
	ATTRKEY		NVARCHAR(256) NOT NULL,
	ATTRORDER	INTEGER	NOT NULL,
	ATTRVAL		NVARCHAR(2000),
	ATTRVALEXT	VARBINARY(MAX)
);
GO

CREATE UNIQUE INDEX DYNA_ATTRS_UIDX 
	ON {SUBST_SCHEMA}.DYNA_ATTRS (OBJID ASC, ATTRKEY ASC, ATTRORDER ASC);
GO

ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS 
	ADD CONSTRAINT PK PRIMARY KEY(ATTRID);
	
ALTER TABLE {SUBST_SCHEMA}.DYNA_ATTRS ADD CONSTRAINT DYNA_ATTR_FK FOREIGN KEY (OBJID)
	REFERENCES {SUBST_SCHEMA}.DYNA_OBJ_REF (OBJID)
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

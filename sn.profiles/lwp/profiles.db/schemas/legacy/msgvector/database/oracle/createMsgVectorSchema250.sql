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

-- ***********************************
-- NOTE TO USERS:
--  Applications that use this schema will have to add their own FK 
--    constraint for the wall table.  The FK constraint will clean 
--    up the wall table when the associated resource is deleted.
-- ***********************************

-- =================================================================
-- Set schema version for message wall feature
-- =================================================================

INSERT INTO {SUBST_SCHEMA}.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_MSG_VECTOR', 4);


-- =================================================================
-- Create Wall Container
-- VECTOR_ID 			- PRIMARY KEY
-- VECTOR_TYPE 		- Essentially the APP_ID of this vector-vector instance
-- VECTOR_RESOURCE_ASSOC_ID	- The resource that this wall is attached to; this is also the FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR  (
	VECTOR_ID CHAR(36) NOT NULL,
	VECTOR_TYPE VARCHAR2(64) NOT NULL,
	VECTOR_RES_ASSOC_ID VARCHAR2(36) NOT NULL,
	VECTOR_RES_ASSOC_TYPE VARCHAR2(64) NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CREATED_BY CHAR(36) NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) NOT NULL,
	ENABLED_P CHAR(1) NOT NULL,
	VISIBILITY VARCHAR(36),
	EDITABILITY VARCHAR(36),
	CONSTRAINT SNMSGV_VECTOR_PK PRIMARY KEY (VECTOR_ID)
) TABLESPACE {SUBST_ORACLE_TABSPACE4K};

-- Only allow one instance of a vector-vector per resource-id per vector^2-type 
CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_VECTOR_RES_ASSOC_UIDX ON {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_RES_ASSOC_ID ASC, VECTOR_TYPE ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE4K};
	
{SUBST_ORACLE_MSGVECTOR_FK_ADD}

-- =================================================================
-- Create Wall Entry
-- ENTRY_ID 	- PRIMARY KEY
-- ENTRY_TYPE	- Allows for different entry entry types for varied rendering
--					Examples: 'message', 'status'
-- WALL_ID		- FORIEGN KEY
-- =================================================================

-- TODO: will not work with CLOB; need to split table
-- CREATE CLUSTER {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_CLST (VECTOR_ID CHAR(36), ENTRY_TYPE VARCHAR2(64), PUBLISHED TIMESTAMP)
-- SIZE 5355
-- TABLESPACE {SUBST_ORACLE_TABSPACE8K}
-- INDEX STORAGE (initial 50000K next 10000K);
--
-- CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_CLST_IDX ON CLUSTER {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_CLST;

-- Base table for oracles
CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY (
	ENTRY_ID CHAR(36) NOT NULL,
	ENTRY_TYPE VARCHAR2(64) NOT NULL,
	VECTOR_ID CHAR(36) NOT NULL,
	PUBLISHED TIMESTAMP NOT NULL,
	PUBLISHED_BY CHAR(36) NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) NOT NULL,
	COMMENT_COUNT INTEGER DEFAULT 0 NOT NULL,
	CONTENT_LOCALE VARCHAR2(20) NOT NULL,
	TITLE VARCHAR2(512),
	SUMMARY VARCHAR2 (4000) NOT NULL,
	CONTENT_TYPE VARCHAR2(256) NOT NULL,
	CONTENT_URL VARCHAR2(1024),
	CONTENT_TEXT CLOB,	
	CONSTRAINT SNMSGV_ENTRY_PK PRIMARY KEY (ENTRY_ID)
) TABLESPACE {SUBST_ORACLE_TABSPACE8K};
-- CLUSTER {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_CLST (VECTOR_ID, ENTRY_TYPE, PUBLISHED);

-- Optimized index to walk data in repeatable order and which will reduce database page fragmentation
-- Also optimized useful for getting most recent entry of a given type (such as status)

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, PUBLISHED DESC, ENTRY_ID ASC) 
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};
	
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, PUBLISHED ASC, ENTRY_ID ASC) 
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

-- Other useful indexes for non-'collection' datatypes built on wall schema
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, LASTUPDATE DESC, ENTRY_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};
	
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_LASTUPDATE_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, LASTUPDATE ASC, ENTRY_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

-- Index to get of all users
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WALKER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, PUBLISHED DESC, ENTRY_ID ASC);

-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD CONSTRAINT SNMSGV_ENTRY_FK FOREIGN KEY (VECTOR_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;


-- =================================================================
-- Create Wall Comment
-- COMMENT_ID 	- PRIMARY KEY
-- ENTRY_ID		- FORIEGN KEY
-- =================================================================

-- TODO: will not work with CLOB; need to split table
-- CREATE CLUSTER {SUBST_SCHEMA}.SNMSGV_CMT_ORDER_CLST ON (ENTRY_ID CHAR(36), COMMENT_TYPE VARCHAR2(64), PUBLISHED_ORDER INTEGER)
-- SIZE 5355
-- TABLESPACE {SUBST_ORACLE_TABSPACE8K}
-- INDEX STORAGE (initial 50000K next 10000K);
--
-- CREATE INDEX {SUBST_SCHEMA}.SNMSGV_CMT_ORDER_CLST_IDX ON CLUSTER {SUBST_SCHEMA}.SNMSGV_CMT_ORDER_CLST;

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT  (
	COMMENT_ID CHAR(36) NOT NULL,
	COMMENT_TYPE VARCHAR2(64) NOT NULL,
	ENTRY_ID CHAR(36) NOT NULL,
	PUBLISHED_ORDER INTEGER DEFAULT 1 NOT NULL,
	PUBLISHED TIMESTAMP NOT NULL,
	PUBLISHED_BY CHAR(36) NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) NOT NULL,
	CONTENT_LOCALE VARCHAR2(20) NOT NULL,
	TITLE VARCHAR2(512),
	SUMMARY VARCHAR2(4000) NOT NULL,
	CONTENT_TYPE VARCHAR2(256) NOT NULL,
	CONTENT_URL VARCHAR2(1024),
	CONTENT_TEXT CLOB,
	CONSTRAINT SNMSGV_COMMENT_PK PRIMARY KEY (COMMENT_ID)
) TABLESPACE {SUBST_ORACLE_TABSPACE8K};
-- CLUSTER {SUBST_SCHEMA}.SNMSGV_CMT_ORDER_CLST (ENTRY_ID, COMMENT_TYPE, PUBLISHED_ORDER);


-- Generic indexes
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_PUBLISHED_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, PUBLISHED ASC, COMMENT_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_PUBLISHED_IDR ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, PUBLISHED DESC, COMMENT_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, LASTUPDATE DESC, COMMENT_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_LASTUPDATE_IDR ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, LASTUPDATE ASC, COMMENT_ID ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

--
-- Optimized for reverse comment walk, retrieve first/last comments
-- Uniqueness constraint: prevents duplicate comment order numbers
--
CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_ORDER_UIDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, COMMENT_TYPE ASC, PUBLISHED_ORDER ASC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE8K};

-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT ADD CONSTRAINT SNMSGV_COMMENT_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;
	
-- =================================================================
-- Create Wall Entry Pointer
--   This table used to store references to 'ENTRY' messages for things like 'status'
-- VECTOR_ID, EP_NAME	- PRIMARY KEY
-- ENTRY_ID				- The resource being referenced
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR  (
	EP_VECTOR_ID CHAR(36) NOT NULL,
	EP_NAME VARCHAR2(64) NOT NULL,
	EP_ENTRY_ID CHAR(36) NOT NULL,
	EP_SET_TIME TIMESTAMP NOT NULL,
	VISIBILITY VARCHAR(36),
	EDITABILITY VARCHAR(36),
	CONSTRAINT SNMSGV_ENTPTR_PK PRIMARY KEY (EP_VECTOR_ID, EP_NAME)
) TABLESPACE {SUBST_ORACLE_TABSPACE4K};

-- Index to get 'updates' of friends
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_PTR_UDIDX ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR (EP_VECTOR_ID ASC, EP_SET_TIME DESC)
	TABLESPACE {SUBST_ORACLE_IDXSPACE4K};

-- Add FK constraint to entry message
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT SNMSGV_ENT_PTR_EFK FOREIGN KEY (EP_ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

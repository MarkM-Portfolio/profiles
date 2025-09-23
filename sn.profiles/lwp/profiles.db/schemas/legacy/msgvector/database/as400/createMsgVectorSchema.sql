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

INSERT INTO {SUBST_SCHEMA}.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_MSG_VECTOR', 6);


-- =================================================================
-- Create Wall Container
-- VECTOR_ID 			- PRIMARY KEY
-- VECTOR_TYPE 		- Essentially the APP_ID of this vector-vector instance
-- VECTOR_RESOURCE_ASSOC_ID	- The resource that this wall is attached to; this is also the FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR  (
	VECTOR_ID CHAR(36) CCSID 1208 NOT NULL,
	VECTOR_TYPE VARCHAR(64) CCSID 1208 NOT NULL,
	VECTOR_RES_ASSOC_ID VARCHAR(36) CCSID 1208 NOT NULL,
	VECTOR_RES_ASSOC_TYPE VARCHAR(64) CCSID 1208 NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CREATED_BY CHAR(36) CCSID 1208 NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) CCSID 1208 NOT NULL,
	ENABLED_P CHAR(1) CCSID 1208 NOT NULL,
	VISIBILITY VARCHAR(36) CCSID 1208,
	EDITABILITY VARCHAR(36) CCSID 1208,
	CONSTRAINT {SUBST_SCHEMA}.SNMSGV_VECTOR_PK PRIMARY KEY (VECTOR_ID)
);

-- Only allow one instance of a vector-vector per resource-id per vector^2-type 
CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_VECTOR_RES_ASSOC_UIDX ON {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_RES_ASSOC_ID ASC, VECTOR_TYPE ASC)
	;
	
{SUBST_AS400_MSGVECTOR_FK_ADD}

-- =================================================================
-- Create Wall Entry
-- ENTRY_ID 	- PRIMARY KEY
-- ENTRY_TYPE	- Allows for different entry entry types for varied rendering
--					Examples: 'message', 'status'
-- WALL_ID		- FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY (
	ENTRY_ID CHAR(36) CCSID 1208 NOT NULL,
	ENTRY_TYPE VARCHAR(64) CCSID 1208 NOT NULL,
	VECTOR_ID CHAR(36) CCSID 1208 NOT NULL,
	PUBLISHED TIMESTAMP NOT NULL,
	PUBLISHED_BY CHAR(36) CCSID 1208 NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) CCSID 1208 NOT NULL,
	COMMENT_COUNT INTEGER DEFAULT 0 NOT NULL,
	CONTENT_LOCALE VARCHAR(20) CCSID 1208 NOT NULL,
	TITLE VARCHAR(512) CCSID 1208,
	SUMMARY VARCHAR(4000) CCSID 1208 NOT NULL,
	CONTENT_TYPE VARCHAR(256) CCSID 1208 NOT NULL,
	CONTENT_URL VARCHAR(1024) CCSID 1208,
	CONTENT_TEXT CLOB(1M) CCSID 1208,	
	EXTPROPS CLOB(1M) CCSID 1208,
	CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENTRY_PK PRIMARY KEY (ENTRY_ID)
);


-- Optimized index to walk data in repeatable order and which will reduce database page fragmentation
-- Also optimized useful for getting most recent entry of a given type (such as status)
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, PUBLISHED ASC, ENTRY_ID ASC)
	;

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, LASTUPDATE ASC, ENTRY_ID ASC)
	;

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WALKER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, PUBLISHED ASC, ENTRY_ID ASC)
	;
	
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WALKER_LU_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, LASTUPDATE ASC, ENTRY_ID ASC)
	;

-- with entry type at end; needed for some queries
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDR2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, PUBLISHED ASC, ENTRY_ID ASC, ENTRY_TYPE ASC)
	;

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_LASTUPDT2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, LASTUPDATE ASC, ENTRY_ID ASC, ENTRY_TYPE ASC)
	;

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WLKR2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (PUBLISHED ASC, ENTRY_ID ASC, ENTRY_TYPE ASC)
	;
	
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WLKR2_LU_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (LASTUPDATE ASC, ENTRY_ID ASC, ENTRY_TYPE ASC)
	;

-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENTRY_FK FOREIGN KEY (VECTOR_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION  ;

-- =================================================================
-- Create the recommendations table
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC (
	REC_ID CHAR(36) CCSID 1208 NOT NULL,
	ENTRY_ID CHAR(36) CCSID 1208 NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CREATEDBY CHAR(36) CCSID 1208 NOT NULL,
	CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENTREC_PK PRIMARY KEY (REC_ID)
);

CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_ENTREC_UDX ON 
	{SUBST_SCHEMA}.SNMSGV_ENTRY_REC (ENTRY_ID ASC, CREATEDBY ASC);

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC ADD CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENTREC_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION  ;	

	
-- =================================================================
-- Create Wall Comment
-- COMMENT_ID 	- PRIMARY KEY
-- ENTRY_ID		- FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT (
	COMMENT_ID CHAR(36) CCSID 1208 NOT NULL,
	COMMENT_TYPE VARCHAR(64) CCSID 1208 NOT NULL,
	ENTRY_ID CHAR(36) CCSID 1208 NOT NULL,
	PUBLISHED_ORDER INTEGER DEFAULT 1 NOT NULL,
	PUBLISHED TIMESTAMP NOT NULL,
	PUBLISHED_BY CHAR(36) CCSID 1208 NOT NULL,
	LASTUPDATE TIMESTAMP NOT NULL,
	LASTUPDATE_BY CHAR(36) CCSID 1208 NOT NULL,
	CONTENT_LOCALE VARCHAR(20) CCSID 1208 NOT NULL,
	TITLE VARCHAR(512) CCSID 1208,
	SUMMARY VARCHAR(4000) CCSID 1208 NOT NULL,
	CONTENT_TYPE VARCHAR(256) CCSID 1208 NOT NULL,
	CONTENT_URL VARCHAR(1024) CCSID 1208,
	CONTENT_TEXT CLOB(1M) CCSID 1208,
	EXTPROPS CLOB(1M) CCSID 1208,
	CONSTRAINT {SUBST_SCHEMA}.SNMSGV_COMMENT_PK PRIMARY KEY (COMMENT_ID)
);


-- Generic indexes
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_PUBLISHED_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, PUBLISHED ASC, COMMENT_ID ASC)
	;

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, LASTUPDATE ASC, COMMENT_ID ASC)
	;

--
-- Optimized for reverse comment walk, retrieve first/last comments
-- Uniqueness constraint: prevents duplicate comment order numbers
--
CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT_ORDER_UIDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, COMMENT_TYPE ASC, PUBLISHED_ORDER ASC)
	;

-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT ADD CONSTRAINT {SUBST_SCHEMA}.SNMSGV_COMMENT_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION  ;


-- =================================================================
-- Create Wall Entry Pointer
--   This table used to store references to 'ENTRY' messages for things like 'status'
-- VECTOR_ID, EP_NAME	- PRIMARY KEY
-- ENTRY_ID				- The resource being referenced
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR  (
	EP_VECTOR_ID CHAR(36) CCSID 1208 NOT NULL,
	EP_NAME VARCHAR(64) CCSID 1208 NOT NULL,
	EP_ENTRY_ID CHAR(36) CCSID 1208 NOT NULL,
	EP_SET_TIME TIMESTAMP NOT NULL,
	VISIBILITY VARCHAR(36) CCSID 1208,
	EDITABILITY VARCHAR(36) CCSID 1208,
	CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENTPTR_PK PRIMARY KEY (EP_VECTOR_ID, EP_NAME)
);

-- Index to get 'updates' of friends
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_PTR_UDIDX ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR (EP_VECTOR_ID ASC, EP_SET_TIME DESC);

-- Add FK constraint to entry message
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT {SUBST_SCHEMA}.SNMSGV_ENT_PTR_EFK FOREIGN KEY (EP_ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION  ;

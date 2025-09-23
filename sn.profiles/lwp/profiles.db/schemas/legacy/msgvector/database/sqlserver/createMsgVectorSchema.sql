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

INSERT INTO {SUBST_SCHEMA}.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_MSG_VECTOR', 6);
GO

-- =================================================================
-- Create Wall Container
-- VECTOR_ID 			- PRIMARY KEY
-- VECTOR_TYPE 		- Essentially the APP_ID of this vector-vector instance
-- VECTOR_RESOURCE_ASSOC_ID	- The resource that this wall is attached to; this is also the FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR  (
	VECTOR_ID NCHAR(36) NOT NULL,
	VECTOR_TYPE NVARCHAR(64) NOT NULL,
	VECTOR_RES_ASSOC_ID NVARCHAR(36) NOT NULL,
	VECTOR_RES_ASSOC_TYPE NVARCHAR(64) NOT NULL,
	CREATED DATETIME NOT NULL,
	CREATED_BY NCHAR(36) NOT NULL,
	LASTUPDATE DATETIME NOT NULL,
	LASTUPDATE_BY NCHAR(36) NOT NULL,
	ENABLED_P NCHAR(1) NOT NULL,
	VISIBILITY VARCHAR(36),
	EDITABILITY VARCHAR(36),
	CONSTRAINT SNMSGV_VECTOR_PK PRIMARY KEY (VECTOR_ID)
);
GO

-- Only allow one instance of a vector-vector per resource-id per vector^2-type 
CREATE UNIQUE INDEX SNMSGV_VECTOR_RES_ASSOC_UIDX ON {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_RES_ASSOC_ID ASC, VECTOR_TYPE ASC);
GO

{SUBST_MSSQL_MSGVECTOR_FK_ADD}	

-- =================================================================
-- Create Wall Entry
-- ENTRY_ID 	- PRIMARY KEY
-- ENTRY_TYPE	- Allows for different entry entry types for varied rendering
--					Examples: 'message', 'status'
-- WALL_ID		- FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY (
	ENTRY_ID NCHAR(36) NOT NULL,
	ENTRY_TYPE NVARCHAR(64) NOT NULL,
	VECTOR_ID NCHAR(36) NOT NULL,
	PUBLISHED DATETIME NOT NULL,
	PUBLISHED_BY NCHAR(36) NOT NULL,
	LASTUPDATE DATETIME NOT NULL,
	LASTUPDATE_BY NCHAR(36) NOT NULL,
	COMMENT_COUNT INTEGER DEFAULT 0 NOT NULL,
	CONTENT_LOCALE NVARCHAR(20) NOT NULL,
	TITLE NVARCHAR(512),
	SUMMARY NVARCHAR (4000) NOT NULL,
	CONTENT_TYPE NVARCHAR(256) NOT NULL,
	CONTENT_URL NVARCHAR(1024),
	CONTENT_TEXT NVARCHAR(MAX),
	EXTPROPS NVARCHAR(MAX)
);
GO

-- Optimized index to walk data in repeatable order and which will reduce database page fragmentation
-- Also optimized useful for getting most recent entry of a given type (such as status)
CREATE INDEX SNMSGV_ENTRY_ORDER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, PUBLISHED ASC, ENTRY_ID ASC);
GO
CREATE CLUSTERED INDEX SNMSGV_ENTRY_ORDER_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID DESC, ENTRY_TYPE DESC, PUBLISHED DESC, ENTRY_ID DESC);
GO

CREATE INDEX SNMSGV_ENTRY_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, LASTUPDATE ASC, ENTRY_ID ASC);
GO
CREATE INDEX SNMSGV_ENTRY_LASTUPDATE_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID DESC, ENTRY_TYPE DESC, LASTUPDATE DESC, ENTRY_ID DESC);
GO

CREATE INDEX SNMSGV_ENT_WALKER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, PUBLISHED ASC, ENTRY_ID ASC);
GO
CREATE INDEX SNMSGV_ENT_WALKER_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE DESC, PUBLISHED DESC, ENTRY_ID DESC);
GO
	
CREATE INDEX SNMSGV_ENT_WALKER_LU_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, LASTUPDATE ASC, ENTRY_ID ASC);
GO
CREATE INDEX SNMSGV_ENT_WALKER_LU_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE DESC, LASTUPDATE DESC, ENTRY_ID DESC);
GO

-- with entry type at end; needed for some queries
CREATE INDEX SNMSGV_ENTRY_ORDR2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, PUBLISHED ASC, ENTRY_ID ASC, ENTRY_TYPE ASC);
GO
CREATE INDEX SNMSGV_ENTRY_ORDR2_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID DESC, PUBLISHED DESC, ENTRY_ID DESC, ENTRY_TYPE DESC);
GO

CREATE INDEX SNMSGV_ENTRY_LASTUPDT2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, LASTUPDATE ASC, ENTRY_ID ASC, ENTRY_TYPE ASC);
GO
CREATE INDEX SNMSGV_ENTRY_LASTUPDT2_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID DESC, LASTUPDATE DESC, ENTRY_ID DESC, ENTRY_TYPE DESC);
GO

CREATE INDEX SNMSGV_ENT_WLKR2_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (PUBLISHED ASC, ENTRY_ID ASC, ENTRY_TYPE ASC);
GO
CREATE INDEX SNMSGV_ENT_WLKR2_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (PUBLISHED DESC, ENTRY_ID DESC, ENTRY_TYPE DESC);
GO
	
CREATE INDEX SNMSGV_ENT_WLKR2_LU_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (LASTUPDATE ASC, ENTRY_ID ASC, ENTRY_TYPE ASC);
GO
CREATE INDEX SNMSGV_ENT_WLKR2_LU_IDR ON {SUBST_SCHEMA}.SNMSGV_ENTRY (LASTUPDATE DESC, ENTRY_ID DESC, ENTRY_TYPE DESC);
GO

-- Add external PK constraint
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY
	ADD CONSTRAINT SNMSGV_ENTRY_PK PRIMARY KEY (ENTRY_ID);
GO


-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD CONSTRAINT SNMSGV_ENTRY_FK FOREIGN KEY (VECTOR_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- =================================================================
-- Create the recommendations table
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC (
	REC_ID NCHAR(36) NOT NULL,
	ENTRY_ID NCHAR(36) NOT NULL,
	CREATED DATETIME NOT NULL,
	CREATEDBY NCHAR(36) NOT NULL,
	CONSTRAINT SNMSGV_ENTREC_PK PRIMARY KEY (REC_ID)
);
GO

CREATE UNIQUE INDEX SNMSGV_ENTREC_UDX ON 
	{SUBST_SCHEMA}.SNMSGV_ENTRY_REC (ENTRY_ID ASC, CREATEDBY ASC);
GO
	
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC ADD CONSTRAINT SNMSGV_ENTREC_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- =================================================================
-- Create Wall Comment
-- COMMENT_ID 	- PRIMARY KEY
-- ENTRY_ID		- FORIEGN KEY
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT (
	COMMENT_ID NCHAR(36) NOT NULL,
	COMMENT_TYPE NVARCHAR(64) NOT NULL,
	ENTRY_ID NCHAR(36) NOT NULL,
	PUBLISHED_ORDER INTEGER DEFAULT 1 NOT NULL,
	PUBLISHED DATETIME NOT NULL,
	PUBLISHED_BY NCHAR(36) NOT NULL,
	LASTUPDATE DATETIME NOT NULL,
	LASTUPDATE_BY NCHAR(36) NOT NULL,
	CONTENT_LOCALE NVARCHAR(20) NOT NULL,
	TITLE NVARCHAR (512),
	SUMMARY NVARCHAR (4000) NOT NULL,
	CONTENT_TYPE NVARCHAR(256) NOT NULL,
	CONTENT_URL NVARCHAR(1024),
	CONTENT_TEXT NVARCHAR(MAX),
	EXTPROPS NVARCHAR(MAX)
);
GO

-- Generic indexes
CREATE INDEX SNMSGV_COMMENT_PUBLISHED_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, PUBLISHED ASC, COMMENT_ID ASC);
GO

CREATE INDEX SNMSGV_COMMENT_PUBLISHED_IDR ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID DESC, PUBLISHED DESC, COMMENT_ID DESC);
GO

CREATE INDEX SNMSGV_COMMENT_LASTUPDATE_IDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, LASTUPDATE ASC, COMMENT_ID ASC);
GO

CREATE INDEX SNMSGV_COMMENT_LASTUPDATE_IDR ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID DESC, LASTUPDATE DESC, COMMENT_ID DESC);
GO

--
-- Optimized for reverse comment walk, retrieve first/last comments
-- Uniqueness constraint: prevents duplicate comment order numbers
--
CREATE UNIQUE CLUSTERED INDEX SNMSGV_COMMENT_ORDER_UIDX ON {SUBST_SCHEMA}.SNMSGV_COMMENT (ENTRY_ID ASC, COMMENT_TYPE ASC, PUBLISHED_ORDER ASC);
GO

-- Add PK constraint
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT 
	ADD CONSTRAINT SNMSGV_COMMENT_PK PRIMARY KEY (COMMENT_ID);
GO

-- Add constraints to keep db clean.  When parent deleted, this will be deleted
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT ADD CONSTRAINT SNMSGV_COMMENT_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

-- =================================================================
-- Create Wall Entry Pointer
--   This table used to store references to 'ENTRY' messages for things like 'status'
-- VECTOR_ID, EP_NAME	- PRIMARY KEY
-- ENTRY_ID				- The resource being referenced
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR  (
	EP_VECTOR_ID NCHAR(36) NOT NULL,
	EP_NAME NVARCHAR(64) NOT NULL,
	EP_ENTRY_ID NCHAR(36) NOT NULL,
	EP_SET_TIME DATETIME NOT NULL,
	VISIBILITY VARCHAR(36),
	EDITABILITY VARCHAR(36),
	CONSTRAINT SNMSGV_ENTPTR_PK PRIMARY KEY (EP_VECTOR_ID, EP_NAME)
);
GO

-- Index to get 'updates' of friends
CREATE INDEX SNMSGV_ENT_PTR_UDIDX ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR (EP_VECTOR_ID ASC, EP_SET_TIME DESC);
GO

-- Add FK constraint to entry message
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT SNMSGV_ENT_PTR_EFK FOREIGN KEY (EP_ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

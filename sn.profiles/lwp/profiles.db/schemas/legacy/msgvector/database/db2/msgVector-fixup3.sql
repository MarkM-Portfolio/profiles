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

-- =================================================================
-- Add index to walk entries
-- =================================================================

-- Fix incorrect index
DROP INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDX@

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (VECTOR_ID ASC, ENTRY_TYPE ASC, PUBLISHED DESC, ENTRY_ID ASC)
	CLUSTER ALLOW REVERSE SCANS@

-- Index to get of all users
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_WALKER_IDX ON {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_TYPE ASC, PUBLISHED DESC, ENTRY_ID ASC)
	ALLOW REVERSE SCANS@

-- Add 'public' flag to 
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD COLUMN PUBLIC_FLAG INTEGER DEFAULT 0 NOT NULL@

-- Fix entry ptr
DROP INDEX {SUBST_SCHEMA}.SNMSGV_ENT_PTR_UDIDX@

CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_PTR_UDIDX ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR (EP_VECTOR_ID ASC, EP_SET_TIME DESC)@

--
-- Update Schema Version
--
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER= 3 WHERE COMPKEY = 'LC_MSG_VECTOR'@

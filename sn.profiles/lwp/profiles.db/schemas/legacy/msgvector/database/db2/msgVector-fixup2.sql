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
-- Create Wall Entry Pointer
--   This table used to store references to 'ENTRY' messages for things like 'status'
-- VECTOR_ID, EP_NAME	- PRIMARY KEY
-- ENTRY_ID				- The resource being referenced
-- =================================================================

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR  (
	EP_VECTOR_ID CHAR(36) NOT NULL,
	EP_NAME VARCHAR(64) NOT NULL,
	EP_ENTRY_ID CHAR(36) NOT NULL,
	EP_SET_TIME TIMESTAMP NOT NULL,
	CONSTRAINT SNMSGV_ENTPTR_PK PRIMARY KEY (EP_VECTOR_ID, EP_NAME)
) IN {SUBST_USERSPACE4K} INDEX IN {SUBST_USERSPACE4K_INDEX}@

-- Index to get 'updates' of friends
CREATE INDEX {SUBST_SCHEMA}.SNMSGV_ENT_PTR_UDIDX ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR (EP_SET_TIME DESC, EP_VECTOR_ID ASC)@

-- Add FK constraint to vector
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT SNMSGV_ENT_PTR_VFK FOREIGN KEY (EP_VECTOR_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

-- Add FK constraint to entry message
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT SNMSGV_ENT_PTR_EFK FOREIGN KEY (EP_ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- Update Schema Version
--
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER= 2, COMPKEY = 'LC_MSG_VECTOR' WHERE COMPKEY = 'LC_MSG_WALL'@

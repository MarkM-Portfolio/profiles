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

--
-- Fix constraints & relations
--

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD COLUMN EXTPROPS CLOB(1M)@
COMMIT@

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT ADD COLUMN EXTPROPS CLOB(1M)@
COMMIT@

CREATE TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC (
	REC_ID CHAR(36) NOT NULL,
	ENTRY_ID CHAR(36) NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	CREATEDBY CHAR(36) NOT NULL,
	CONSTRAINT SNMSGV_ENTREC_PK PRIMARY KEY (REC_ID)
) IN {SUBST_USERSPACE4K} INDEX IN {SUBST_USERSPACE4K_INDEX}@

CREATE UNIQUE INDEX {SUBST_SCHEMA}.SNMSGV_ENTREC_UDX ON 
	{SUBST_SCHEMA}.SNMSGV_ENTRY_REC (ENTRY_ID ASC, CREATEDBY ASC)@

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC ADD CONSTRAINT SNMSGV_ENTREC_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

COMMIT@

--
-- Update Schema Version
--
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER= 6 WHERE COMPKEY = 'LC_MSG_VECTOR'@

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

{SUBST_ORACLE_MSGVECTOR_FK_ADD}

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY ADD CONSTRAINT SNMSGV_ENTRY_FK FOREIGN KEY (VECTOR_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_VECTOR (VECTOR_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT ADD CONSTRAINT SNMSGV_COMMENT_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;
	
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR ADD CONSTRAINT SNMSGV_ENT_PTR_EFK FOREIGN KEY (EP_ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC ADD CONSTRAINT SNMSGV_ENTREC_FK FOREIGN KEY (ENTRY_ID) 
	REFERENCES {SUBST_SCHEMA}.SNMSGV_ENTRY (ENTRY_ID) 
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

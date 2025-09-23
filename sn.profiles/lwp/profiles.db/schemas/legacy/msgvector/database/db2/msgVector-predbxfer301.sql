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

{SUBST_DB2_MSGVECTOR_FK_REMOVE}
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY DROP CONSTRAINT SNMSGV_ENTRY_FK@
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT DROP CONSTRAINT SNMSGV_COMMENT_FK@
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENT_PTR DROP CONSTRAINT SNMSGV_ENT_PTR_EFK@
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY_REC DROP CONSTRAINT SNMSGV_ENTREC_FK@

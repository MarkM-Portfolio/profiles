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

reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_VECTOR@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_ENTRY@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_COMMENT@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_ENT_PTR@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_ENTRY_REC@

reorg table {SUBST_SCHEMA}.SNMSGV_VECTOR use {SUBST_TEMPSPACE4K}@
reorg table {SUBST_SCHEMA}.SNMSGV_ENTRY index {SUBST_SCHEMA}.SNMSGV_ENTRY_ORDER_IDX use {SUBST_TEMPSPACE8K}@
reorg table {SUBST_SCHEMA}.SNMSGV_COMMENT index {SUBST_SCHEMA}.SNMSGV_COMMENT_ORDER_UIDX use {SUBST_TEMPSPACE8K}@
reorg table {SUBST_SCHEMA}.SNMSGV_ENT_PTR use {SUBST_TEMPSPACE4K}@
reorg table {SUBST_SCHEMA}.SNMSGV_ENTRY_REC use {SUBST_TEMPSPACE4K}@

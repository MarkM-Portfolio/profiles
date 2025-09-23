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

reorg indexes all for table {SUBST_SCHEMA}.DYNA_DEFS@
reorg indexes all for table {SUBST_SCHEMA}.DYNA_OBJ_REF@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_COMMENT@
reorg indexes all for table {SUBST_SCHEMA}.SNMSGV_ENT_PTR@

reorg table {SUBST_SCHEMA}.DYNA_DEFS use {SUBST_TEMPSPACE4K}@
reorg table {SUBST_SCHEMA}.DYNA_OBJ_REF use {SUBST_TEMPSPACE4K}@
reorg table {SUBST_SCHEMA}.DYNA_ATTRS use {SUBST_TEMPSPACE4K}@

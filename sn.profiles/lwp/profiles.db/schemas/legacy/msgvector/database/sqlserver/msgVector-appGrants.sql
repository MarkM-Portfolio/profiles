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

GRANT DELETE, INSERT, SELECT, UPDATE, CONTROL ON {SUBST_SCHEMA}.SNMSGV_VECTOR TO {SUBST_MSSQLUSER};
GO
GRANT DELETE, INSERT, SELECT, UPDATE, CONTROL ON {SUBST_SCHEMA}.SNMSGV_ENTRY TO {SUBST_MSSQLUSER};
GO
GRANT DELETE, INSERT, SELECT, UPDATE, CONTROL ON {SUBST_SCHEMA}.SNMSGV_COMMENT TO {SUBST_MSSQLUSER};
GO
GRANT DELETE, INSERT, SELECT, UPDATE, CONTROL ON {SUBST_SCHEMA}.SNMSGV_ENT_PTR TO {SUBST_MSSQLUSER};
GO
GRANT DELETE, INSERT, SELECT, UPDATE, CONTROL ON {SUBST_SCHEMA}.SNMSGV_ENTRY_REC TO {SUBST_MSSQLUSER};
GO

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

-- this is a subset of the postdbxfer

SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_VECTOR OFF@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_ENTRY OFF@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_COMMENT OFF@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_ENT_PTR OFF@

SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_VECTOR IMMEDIATE CHECKED@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_ENTRY IMMEDIATE CHECKED@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_COMMENT IMMEDIATE CHECKED@
SET INTEGRITY FOR {SUBST_SCHEMA}.SNMSGV_ENT_PTR IMMEDIATE CHECKED@

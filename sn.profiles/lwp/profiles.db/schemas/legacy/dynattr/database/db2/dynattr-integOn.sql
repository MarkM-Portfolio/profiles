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

SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_DEFS OFF@
SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_OBJ_REF OFF@
SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_ATTRS OFF@

SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_DEFS IMMEDIATE CHECKED@
SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_OBJ_REF IMMEDIATE CHECKED@
SET INTEGRITY FOR {SUBST_SCHEMA}.DYNA_ATTRS IMMEDIATE CHECKED@


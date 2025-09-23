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

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_ENTRY DROP CONSTRAINT DYNA_DEF_FK;
GO
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT DROP CONSTRAINT DYNA_ATTR_FK;
GO

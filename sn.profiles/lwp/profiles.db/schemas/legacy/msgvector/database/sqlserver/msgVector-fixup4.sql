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
-- Fixup msgvector
--
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR DROP CONSTRAINT DF__SNMSGV_VE__PUBLI__2E1BDC42;
GO
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR DROP COLUMN PUBLIC_FLAG;
GO
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR ADD VISIBILITY VARCHAR(36);
GO
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_VECTOR ADD EDITABILITY VARCHAR(36);
GO

--
-- Fixup comment
-- 

ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT DROP CONSTRAINT DF__SNMSGV_CO__PUBLI__35BCFE0A;
GO
DROP INDEX {SUBST_SCHEMA}.SNMSGV_COMMENT.SNMSGV_COMMENT_ORDER_REV_UIDX;
GO
ALTER TABLE {SUBST_SCHEMA}.SNMSGV_COMMENT DROP COLUMN PUBLISHED_ORDER_REV;
GO


--
-- Update Schema Version
--
UPDATE {SUBST_SCHEMA}.SNCORE_SCHEMA SET DBSCHEMAVER= 4 WHERE COMPKEY = 'LC_MSG_VECTOR';
GO

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2013                                   
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB;
GO

------
-- table SNMSGV_ENT_PTR
------
ALTER TABLE EMPINST.SNMSGV_ENT_PTR DROP CONSTRAINT SNMSGV_ENT_PTR_EFK; 
GO

DROP INDEX SNMSGV_ENT_PTR_UDIDX ON EMPINST.SNMSGV_ENT_PTR;
GO

DROP TABLE EMPINST.SNMSGV_ENT_PTR;
GO

------
-- table SNMSGV_COMMENT
------
ALTER TABLE EMPINST.SNMSGV_COMMENT DROP CONSTRAINT SNMSGV_COMMENT_FK;
GO

DROP INDEX SNMSGV_COMMENT_ORDER_UIDX ON EMPINST.SNMSGV_COMMENT;
GO

DROP INDEX SNMSGV_COMMENT_LASTUPDATE_IDX ON EMPINST.SNMSGV_COMMENT;
GO

DROP INDEX SNMSGV_COMMENT_PUBLISHED_IDX ON EMPINST.SNMSGV_COMMENT;
GO

DROP TABLE EMPINST.SNMSGV_COMMENT;
GO

------
-- table SNMSGV_ENTRY_REC
------
ALTER TABLE EMPINST.SNMSGV_ENTRY_REC DROP CONSTRAINT SNMSGV_ENTREC_FK;
GO

DROP INDEX SNMSGV_ENTREC_UDX ON EMPINST.SNMSGV_ENTRY_REC;
GO

DROP TABLE EMPINST.SNMSGV_ENTRY_REC;
GO

------
-- table SNMSGV_ENTRY
------
ALTER TABLE EMPINST.SNMSGV_ENTRY DROP CONSTRAINT SNMSGV_ENTRY_FK;
GO

DROP INDEX SNMSGV_ENT_WLKR2_LU_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENT_WLKR2_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENTRY_LASTUPDT2_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENTRY_ORDR2_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENT_WALKER_LU_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENT_WALKER_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENTRY_LASTUPDATE_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP INDEX SNMSGV_ENTRY_ORDER_IDX ON EMPINST.SNMSGV_ENTRY;
GO

DROP TABLE EMPINST.SNMSGV_ENTRY;
GO

------
-- table SNMSGV_VECTOR
------
ALTER TABLE EMPINST.SNMSGV_VECTOR DROP CONSTRAINT SNMSGV_VECTOR_FK;
GO

DROP INDEX SNMSGV_VECTOR_RES_ASSOC_UIDX ON EMPINST.SNMSGV_VECTOR;
GO

DROP TABLE EMPINST.SNMSGV_VECTOR;
GO

------
-- TODO: Anything to do for this?
------
-- INSERT INTO EMPINST.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_MSG_VECTOR', 6);
-- GO

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 37, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
GO




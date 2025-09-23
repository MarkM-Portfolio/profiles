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
CONNECT TO PEOPLEDB@

------
-- table SNMSGV_ENT_PTR
------
ALTER TABLE EMPINST.SNMSGV_ENT_PTR DROP CONSTRAINT SNMSGV_ENT_PTR_EFK@ 
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENT_PTR_UDIDX@
COMMIT@

DROP TABLE EMPINST.SNMSGV_ENT_PTR@
COMMIT@

------
-- table SNMSGV_COMMENT
------
ALTER TABLE EMPINST.SNMSGV_COMMENT DROP CONSTRAINT SNMSGV_COMMENT_FK@
COMMIT@

DROP INDEX EMPINST.SNMSGV_COMMENT_ORDER_UIDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_COMMENT_LASTUPDATE_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_COMMENT_PUBLISHED_IDX@
COMMIT@

DROP TABLE EMPINST.SNMSGV_COMMENT@
COMMIT@

------
-- table SNMSGV_ENTRY_REC
------
ALTER TABLE EMPINST.SNMSGV_ENTRY_REC DROP CONSTRAINT SNMSGV_ENTREC_FK@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENTREC_UDX@
COMMIT@

DROP TABLE EMPINST.SNMSGV_ENTRY_REC@
COMMIT@

------
-- table SNMSGV_ENTRY
------
ALTER TABLE EMPINST.SNMSGV_ENTRY DROP CONSTRAINT SNMSGV_ENTRY_FK@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENT_WLKR2_LU_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENT_WLKR2_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENTRY_LASTUPDT2_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENTRY_ORDR2_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENT_WALKER_LU_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENT_WALKER_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENTRY_LASTUPDATE_IDX@
COMMIT@

DROP INDEX EMPINST.SNMSGV_ENTRY_ORDER_IDX@
COMMIT@

DROP TABLE EMPINST.SNMSGV_ENTRY@
COMMIT@

------
-- table SNMSGV_VECTOR
------
ALTER TABLE EMPINST.SNMSGV_VECTOR DROP CONSTRAINT SNMSGV_VECTOR_FK@
COMMIT@

DROP INDEX EMPINST.SNMSGV_VECTOR_RES_ASSOC_UIDX@
COMMIT@

DROP TABLE EMPINST.SNMSGV_VECTOR@
COMMIT@

------
-- TODO: Anything to do for this?
------
-- INSERT INTO EMPINST.SNCORE_SCHEMA (COMPKEY, DBSCHEMAVER) VALUES ('LC_MSG_VECTOR', 6)@
-- COMMIT@

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 37, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------
CONNECT RESET@



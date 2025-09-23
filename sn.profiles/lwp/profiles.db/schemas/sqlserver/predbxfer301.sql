-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

USE PEOPLEDB;
GO

DISABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

DELETE FROM EMPINST.SNPROF_SCHEMA;
GO
DELETE FROM EMPINST.SNCORE_SCHEMA;
GO
DELETE FROM EMPINST.PROF_CONSTANTS;
GO

ALTER TABLE EMPINST.USER_STATE_LAIDX DROP CONSTRAINT USRSTATE_DEF_FK;
ALTER TABLE EMPINST.USER_STATE_LAIDX DROP CONSTRAINT USRSTATE_ATTR_FK;
GO

ALTER TABLE EMPINST.USER_ORGS_LAIDX DROP CONSTRAINT USRORGS_DEF_FK;
ALTER TABLE EMPINST.USER_ORGS_LAIDX DROP CONSTRAINT USRORGS_ATTR_FK;
GO

{include.msgVector-predbxfer301.sql}

{include.dynattr-predbxfer30.sql}

-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO

DELETE FROM EMPINST.USER_STATE_LAIDX;
GO

ALTER TABLE EMPINST.USER_STATE_LAIDX DROP CONSTRAINT USRSTATE_DEF_FK;
GO

{include.dynaDefsFixUp04.sql}

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

--
-- Add state field for lookaside index tables
--
UPDATE EMPINST.PROF_CONSTANTS SET PROF_PROPERTY_VALUE = 'inconsistent' WHERE PROF_PROPERTY_KEY = 'LA_IDX_STATE';
GO

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 26 WHERE COMPKEY='Profiles';
GO

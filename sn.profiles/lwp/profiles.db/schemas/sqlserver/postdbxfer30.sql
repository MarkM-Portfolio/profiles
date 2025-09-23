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

ENABLE TRIGGER ALL ON EMPINST.EMP_DRAFT;
GO

--
-- Foriegn key constraint to reference field which contains value
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_ATTR_FK FOREIGN KEY (ATTRID) 
	REFERENCES EMPINST.DYNA_ATTRS(ATTRID)
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

--
-- Foriegn key constraint to reference field which contains value
--
ALTER TABLE EMPINST.USER_ORGS_LAIDX ADD CONSTRAINT USRORGS_ATTR_FK FOREIGN KEY (ATTRID) 
	REFERENCES EMPINST.DYNA_ATTRS(ATTRID)
	ON DELETE CASCADE ON UPDATE NO ACTION;
GO

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_ORGS_LAIDX ADD CONSTRAINT USRORGS_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE NO ACTION ON UPDATE NO ACTION;
GO	


{include.msgVector-postdbxfer30.sql}

{include.dynattr-postdbxfer30.sql}

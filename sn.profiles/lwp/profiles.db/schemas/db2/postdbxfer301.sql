-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

CREATE TRIGGER "EMPINST"."T_EXT_DRAFT_SEQ" NO CASCADE BEFORE INSERT ON "EMPINST"."PROFILE_EXT_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL             
		 SET N.PROF_UPDATE_SEQUENCE = NEXTVAL FOR "EMPINST"."EXT_DRAFT_SEQ"@  

CREATE TRIGGER "EMPINST"."T_EMP_DRAFT_SEQ" NO CASCADE BEFORE INSERT ON "EMPINST"."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 SET N.PROF_UPDATE_SEQUENCE = NEXTVAL FOR "EMPINST"."EMP_DRAFT_SEQ"@

CREATE TRIGGER "EMPINST"."T_EMP_INS" AFTER INSERT ON "EMPINST"."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 INSERT INTO "EMPINST"."CHG_EMP_DRAFT" VALUES (NEXTVAL FOR "EMPINST"."CHG_EMP_DRAFT_SEQ", 0, 
			CURRENT_DATE, 'I', N.PROF_UPDATE_SEQUENCE, N.PROF_KEY )@

CREATE TRIGGER "EMPINST"."T_EMP_DEL" AFTER DELETE ON "EMPINST"."EMP_DRAFT" REFERENCING OLD AS N 
	FOR EACH ROW MODE DB2SQL
		 INSERT INTO "EMPINST"."CHG_EMP_DRAFT" VALUES (NEXTVAL FOR "EMPINST"."CHG_EMP_DRAFT_SEQ", 0, 
			CURRENT_DATE, 'D', N.PROF_UPDATE_SEQUENCE, N.PROF_KEY )@

CREATE TRIGGER "EMPINST"."T_EMP_UPD" AFTER UPDATE ON "EMPINST"."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW MODE DB2SQL 
		 INSERT INTO "EMPINST"."CHG_EMP_DRAFT" VALUES (NEXTVAL FOR "EMPINST"."CHG_EMP_DRAFT_SEQ", 0, 
			CURRENT_DATE, 'U', N.PROF_UPDATE_SEQUENCE, N.PROF_KEY )@
COMMIT@

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- Foriegn key constraint to reference field which contains value
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_ATTR_FK FOREIGN KEY (ATTRID) 
	REFERENCES EMPINST.DYNA_ATTRS(ATTRID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@
COMMIT@


--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_ORGS_LAIDX ADD CONSTRAINT USRORGS_DEF_FK FOREIGN KEY (DEFID) 
	REFERENCES EMPINST.DYNA_DEFS(DEFID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

--
-- Foriegn key constraint to reference field which contains value
--
ALTER TABLE EMPINST.USER_ORGS_LAIDX ADD CONSTRAINT USRORGS_ATTR_FK FOREIGN KEY (ATTRID) 
	REFERENCES EMPINST.DYNA_ATTRS(ATTRID)
	ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@


{include.msgVector-postdbxfer301.sql}

{include.dynattr-postdbxfer30.sql}

COMMIT@
CONNECT RESET@


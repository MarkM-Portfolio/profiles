-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2015                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
-- CREATE THE TRIGGERS
--------------------------------------
CREATE TRIGGER EMPINST."T_EXT_DRAFT_SEQ" BEFORE INSERT ON EMPINST."PROFILE_EXT_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW
		BEGIN
		 SELECT EMPINST.EXT_DRAFT_SEQ.NEXTVAL INTO :N.PROF_UPDATE_SEQUENCE FROM DUAL;
		END;
/


CREATE TRIGGER EMPINST."T_EMP_DRAFT_SEQ" BEFORE INSERT ON EMPINST."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW 
		BEGIN
		 SELECT EMPINST.EMP_DRAFT_SEQ.NEXTVAL INTO :N.PROF_UPDATE_SEQUENCE FROM DUAL;
		END;
/


CREATE TRIGGER EMPINST."T_EMP_INS" AFTER INSERT ON EMPINST."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW
		BEGIN 
		 INSERT INTO EMPINST."CHG_EMP_DRAFT" (IBMSNAP_COMMITSEQ, IBMSNAP_INTENTSEQ, IBMSNAP_OPERATION,
                                                 IBMSNAP_LOGMARKER, PROF_UPDATE_SEQUENCE, PROF_KEY)
 		 VALUES (LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ1.NEXTVAL),20,'0'), LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ2.NEXTVAL),20,'0'), 'I', SYSDATE, :N.PROF_UPDATE_SEQUENCE, :N.PROF_KEY);
		END;
/
		 

CREATE TRIGGER EMPINST."T_EMP_DEL" AFTER DELETE ON EMPINST."EMP_DRAFT" REFERENCING OLD AS N 
	FOR EACH ROW 
		 BEGIN 
		 INSERT INTO EMPINST."CHG_EMP_DRAFT" (IBMSNAP_COMMITSEQ, IBMSNAP_INTENTSEQ, IBMSNAP_OPERATION,
                                                 IBMSNAP_LOGMARKER, PROF_UPDATE_SEQUENCE, PROF_KEY)
 		 VALUES (LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ1.NEXTVAL),20,'0'), LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ2.NEXTVAL),20,'0'), 'D', SYSDATE, :N.PROF_UPDATE_SEQUENCE, :N.PROF_KEY);
		END;
/

CREATE TRIGGER EMPINST."T_EMP_UPD" AFTER UPDATE ON EMPINST."EMP_DRAFT" REFERENCING NEW AS N 
	FOR EACH ROW 
		 BEGIN 
		 INSERT INTO EMPINST."CHG_EMP_DRAFT" (IBMSNAP_COMMITSEQ, IBMSNAP_INTENTSEQ, IBMSNAP_OPERATION,
                                                 IBMSNAP_LOGMARKER, PROF_UPDATE_SEQUENCE, PROF_KEY)
 		 VALUES (LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ1.NEXTVAL),20,'0'), LPAD(TO_CHAR(EMPINST.CHG_EMP_DRAFT_SEQ2.NEXTVAL),20,'0'), 'U', SYSDATE, :N.PROF_UPDATE_SEQUENCE, :N.PROF_KEY);
		END;
/

COMMIT;
QUIT;

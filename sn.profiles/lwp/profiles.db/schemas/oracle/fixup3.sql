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

-- 5724-S68                                                          
-- DDL Statements for table "EMPINST"."EMPLOYEE"
------------------------------------------------

ALTER TABLE "EMPINST"."EMPLOYEE" ADD "PROF_SOURCE_URL" VARCHAR2(256);


------------------------------------------------
-- DDL Statements for table "EMPINST"."CHG_EMP_DRAFT"
------------------------------------------------

DROP TRIGGER EMPINST."T_EMP_INS";
DROP TRIGGER EMPINST."T_EMP_DEL";
DROP TRIGGER EMPINST."T_EMP_UPD";

DROP TABLE EMPINST."CHG_EMP_DRAFT";

CREATE TABLE EMPINST."CHG_EMP_DRAFT" ( 
  IBMSNAP_COMMITSEQ NUMBER(19,0) NULL, 
  IBMSNAP_INTENTSEQ NUMBER(19,0) NOT NULL,
  IBMSNAP_OPERATION CHAR(1) NOT NULL,	 
  IBMSNAP_LOGMARKER DATE NOT NULL, 
  PROF_UPDATE_SEQUENCE NUMBER(19,0) NOT NULL,
  PROF_KEY VARCHAR2(36) NOT NULL)
  TABLESPACE PROFREGTABSPACE;

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
------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 3 WHERE COMPKEY='Profiles';


COMMIT;

QUIT;

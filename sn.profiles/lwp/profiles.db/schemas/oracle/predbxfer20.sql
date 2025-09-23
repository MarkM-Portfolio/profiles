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
-- DROP ALL TRIGGERS --------
DROP TRIGGER EMPINST."T_EXT_DRAFT_SEQ";
DROP TRIGGER EMPINST."T_PEOPLE_TAG_SEQ";
DROP TRIGGER EMPINST.T_EMPLOYEE_INSRT;
DROP TRIGGER EMPINST."T_EMPLOYEE_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_UID_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_GW_MAIL_UPDT";
DROP TRIGGER EMPINST."T_EMPLOYEE_LOGIN_UPDT";
DROP TRIGGER EMPINST."T_PHOTO_INSRT";
DROP TRIGGER EMPINST."T_PHOTO_UPDT";
DROP TRIGGER EMPINST."T_PRONOUNCE_INSRT";
DROP TRIGGER EMPINST."T_PRONOUNCE_UPDT";
DROP TRIGGER EMPINST."T_EMP_DRAFT_SEQ";
DROP TRIGGER EMPINST."T_EMP_INS";
DROP TRIGGER EMPINST."T_EMP_DEL";
DROP TRIGGER EMPINST."T_EMP_UPD";

DELETE FROM EMPINST.SNPROF_SCHEMA;

COMMIT;
QUIT;

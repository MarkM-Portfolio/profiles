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

-- Remove generated expressions prior to importing database records
-- to prevent errors when attempting to write into generated fields

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_MAIL_LOWER DROP EXPRESSION @ 
COMMIT@

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_LOGIN_LOWER DROP EXPRESSION @
COMMIT@

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_GW_EMAIL_LOWER DROP EXPRESSION @    
COMMIT@

ALTER TABLE EMPINST.EMPLOYEE ALTER COLUMN PROF_UID_LOWER DROP EXPRESSION @ 
COMMIT@


DROP TRIGGER "EMPINST "."T_EXT_DRAFT_SEQ"@
DROP TRIGGER "EMPINST "."T_EMPLOYEE_INSRT"@ 
DROP TRIGGER "EMPINST "."T_EMPLOYEE_UPDT"@
DROP TRIGGER "EMPINST "."T_PHOTO_INSRT"@ 
DROP TRIGGER "EMPINST "."T_PHOTO_UPDT"@ 
DROP TRIGGER "EMPINST "."T_PRONOUNCE_INSRT"@ 
DROP TRIGGER "EMPINST "."T_PRONOUNCE_UPDT"@ 
DROP TRIGGER "EMPINST "."T_EMP_DRAFT_SEQ"@ 
DROP TRIGGER "EMPINST "."T_EMP_INS"@ 
DROP TRIGGER "EMPINST "."T_EMP_DEL"@ 
DROP TRIGGER "EMPINST "."T_EMP_UPD"@

DELETE FROM EMPINST.SNPROF_SCHEMA@
COMMIT@
CONNECT RESET@

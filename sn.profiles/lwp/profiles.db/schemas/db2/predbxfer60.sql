-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2016                                          
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@


DROP TRIGGER EMPINST.T_EXT_DRAFT_SEQ@
DROP TRIGGER EMPINST.T_EMP_DRAFT_SEQ@
DROP TRIGGER EMPINST.T_EMP_INS@ 
DROP TRIGGER EMPINST.T_EMP_DEL@ 
DROP TRIGGER EMPINST.T_EMP_UPD@
COMMIT@

-- DROP createDb inserted values
DELETE FROM EMPINST.SNPROF_SCHEMA@
DELETE FROM EMPINST.PROF_CONSTANTS@
DELETE FROM EMPINST.TENANT@
COMMIT@

COMMIT@
CONNECT RESET@

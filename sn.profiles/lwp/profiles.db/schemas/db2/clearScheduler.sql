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

CONNECT TO PEOPLEDB@

DELETE FROM EMPINST.PROFILES_SCHEDULER_TASK@
COMMIT@
DELETE FROM EMPINST.PROFILES_SCHEDULER_TREG@
COMMIT@
DELETE FROM EMPINST.PROFILES_SCHEDULER_LMGR@
COMMIT@
DELETE FROM EMPINST.PROFILES_SCHEDULER_LMPR@
COMMIT@
 
CONNECT RESET@
-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2014                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 


DELETE FROM EMPINST.PROFILES_SCHEDULER_TASK;
COMMIT;
DELETE FROM EMPINST.PROFILES_SCHEDULER_TREG;
COMMIT;
DELETE FROM EMPINST.PROFILES_SCHEDULER_LMGR;
COMMIT;
DELETE FROM EMPINST.PROFILES_SCHEDULER_LMPR;
COMMIT;

------
-- Disconnect
------

QUIT;

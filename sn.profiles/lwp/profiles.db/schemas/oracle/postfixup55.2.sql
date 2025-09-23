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

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER='55.2', RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';

COMMIT;

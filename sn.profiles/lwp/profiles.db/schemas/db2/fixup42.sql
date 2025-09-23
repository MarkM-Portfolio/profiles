-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2013                                   
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

--====
-- PROFILE_EXTENSIONS: Extension attribute labels
--====

ALTER TABLE EMPINST.PROFILE_EXTENSIONS
	ADD COLUMN SUPPORT_LABEL SMALLINT DEFAULT 0 NOT NULL@

------
-- Update schema version
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 42, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------
CONNECT RESET@



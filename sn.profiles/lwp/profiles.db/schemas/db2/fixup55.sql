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

CONNECT TO PEOPLEDB@

------
-- add pre/post schema version info
------

ALTER TABLE EMPINST.SNPROF_SCHEMA ADD COLUMN PRESCHEMAVER VARCHAR(10) DEFAULT '0' NOT NULL@
ALTER TABLE EMPINST.SNPROF_SCHEMA ADD COLUMN POSTSCHEMAVER VARCHAR(10) DEFAULT '0' NOT NULL@
COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles'@
COMMIT@

------
-- Disconnect
------

CONNECT RESET@

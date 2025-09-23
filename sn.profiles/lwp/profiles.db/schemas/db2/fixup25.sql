-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2013                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@

-- Rebuild
DROP INDEX EMPINST.USRSTATE_CONS_VALB@
DROP INDEX EMPINST.USRSTATE_CONS_VALS@
DROP INDEX EMPINST.USRSTATE_IDX@

--
-- Constrain to one value per user
--
CREATE UNIQUE INDEX USRSTATE_CONS_VALB 
	ON EMPINST.USER_STATE_LAIDX (OBJID ASC)
	INCLUDE (OBJIDSTR, USRSTATE, USRSTATE_UPDATED)
	ALLOW REVERSE SCANS@
	
CREATE UNIQUE INDEX USRSTATE_CONS_VALS 
	ON EMPINST.USER_STATE_LAIDX (OBJIDSTR ASC)
	INCLUDE (OBJID, USRSTATE, USRSTATE_UPDATED)
	ALLOW REVERSE SCANS@

--
-- Main index for scanning
--
CREATE INDEX USRSTATE_IDX
	ON EMPINST.USER_STATE_LAIDX (USRSTATE ASC, USRSTATE_UPDATED ASC, OBJID ASC, OBJIDSTR ASC)
	ALLOW REVERSE SCANS@

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 25 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

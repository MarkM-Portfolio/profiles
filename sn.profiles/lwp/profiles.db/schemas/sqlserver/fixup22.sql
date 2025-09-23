-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2009, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB
GO

--
-- Platform Command Table changes
--
	
ALTER TABLE EMPINST.USER_PLATFORM_EVENTS_INDEX 
ADD CONSTRAINT IDENTITY(0,1);
GO


DROP TABLE "EMPINST"."USER_PLATFORM_EVENTS_INDEX"
GO

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 22 WHERE COMPKEY='Profiles';
GO

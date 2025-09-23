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
USE PEOPLEDB
GO

DROP VIEW EMPINST.SNCORE_PERSON;
GO

DROP VIEW EMPINST.EMPLOYEE_SNCORE;
GO

CREATE VIEW EMPINST.SNCORE_PERSON (SNC_INTERNAL_ID, SNC_IDKEY, SNC_EMAIL_LOWER, SNC_DISPLAY_NAME)
  	AS SELECT PROF_KEY, PROF_GUID, PROF_MAIL_LOWER, PROF_DISPLAY_NAME FROM EMPINST.EMPLOYEE;
GO

-- Update schema version
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 9 WHERE COMPKEY='Profiles';
GO

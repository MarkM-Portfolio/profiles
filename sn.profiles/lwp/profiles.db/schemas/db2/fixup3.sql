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

ALTER TABLE "EMPINST"."EMPLOYEE" ADD COLUMN "PROF_SOURCE_URL" VARCHAR(256)@

ALTER TABLE "EMPINST"."PEOPLE_TAG" DROP PRIMARY KEY@


------------------------------------------------
-- update schema to current version
------------------------------------------------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 3 WHERE COMPKEY='Profiles'@

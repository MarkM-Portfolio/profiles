-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2012, 2014                             
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 
-- 5724-S68                                                          
SET CURRENT SCHEMA EMPINST;
------
-- EMP_DRAFT: drop incorrect constraint that prevented multiple draft attributes
------
ALTER TABLE EMPINST.EMP_DRAFT DROP CONSTRAINT EMPDRAFT_TENT_UK;
COMMIT;

------
-- CHG_EMP_DRAFT: drop incorrect constraint that prevented multiple draft attributes
------
ALTER TABLE EMPINST.CHG_EMP_DRAFT DROP CONSTRAINT CHEMPDRFT_TENT_UK;
COMMIT;

------
-- Update schema versions
------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 36, RELEASEVER='4.5.0.0' WHERE COMPKEY='Profiles';
COMMIT;


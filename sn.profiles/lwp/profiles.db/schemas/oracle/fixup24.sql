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
{include.dynaDefsFixUp03.sql}

{include.msgVector-fixup5.sql}

--
-- Add state field for lookaside index tables
--
INSERT INTO EMPINST.PROF_CONSTANTS VALUES ('LA_IDX_STATE', 'inconsistent');

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 24 WHERE COMPKEY='Profiles';

COMMIT;
QUIT;

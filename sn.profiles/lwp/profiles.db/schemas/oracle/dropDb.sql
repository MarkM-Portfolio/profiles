-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2012                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
DROP USER PROFUSER CASCADE;
DROP ROLE PROFUSER_ROLE;

DROP USER EMPINST CASCADE; 
DROP TABLESPACE PROFREGTABSPACE INCLUDING CONTENTS AND DATAFILES CASCADE CONSTRAINTS;
DROP TABLESPACE PROFINDEXTABSPACE INCLUDING CONTENTS AND DATAFILES CASCADE CONSTRAINTS;
QUIT;

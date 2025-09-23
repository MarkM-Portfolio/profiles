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
-- Platform Command Table changes
--
	
CREATE SEQUENCE "EMPINST"."USER_PLATFORM_EVENT_INDEX_SEQ"
	START WITH 0
	INCREMENT BY 1
	NOMAXVALUE
	NOCYCLE
	MINVALUE 0
	CACHE 20;

DROP TABLE "EMPINST"."USER_PLATFORM_EVENTS_INDEX";


-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 22 WHERE COMPKEY='Profiles';

COMMIT;
QUIT;

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
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------

DROP INDEX EMPINST.SURNAME_UDX;

DROP INDEX EMPINST.SURNAME_IDX;

ALTER TABLE EMPINST.SURNAME ADD CONSTRAINT SURNAME_PK PRIMARY KEY (PROF_KEY, PROF_SURNAME);

CREATE INDEX EMPINST.SURNAME_IDX ON EMPINST.SURNAME (PROF_SURNAME ASC, PROF_KEY ASC)
	TABLESPACE PROFINDEXTABSPACE;


------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

DROP INDEX EMPINST.GIVEN_NAME_UDX;

DROP INDEX EMPINST.GIVEN_NAME_IDX;

ALTER TABLE EMPINST.GIVEN_NAME ADD CONSTRAINT GIVEN_NAME_PK PRIMARY KEY (PROF_KEY, PROF_GIVENNAME);

CREATE INDEX EMPINST.GIVENNAME_IDX ON EMPINST.GIVEN_NAME (PROF_GIVENNAME ASC, PROF_KEY ASC)
	TABLESPACE PROFINDEXTABSPACE;


-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 27 WHERE COMPKEY='Profiles';


COMMIT;
QUIT;


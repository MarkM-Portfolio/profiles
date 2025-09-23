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

!db2set DB2_INLIST_TO_NLJN=YES@
!db2set DB2_EVALUNCOMMITTED=YES@


------------------------------------------------
-- DDL Statements for table "EMPINST"."SURNAME"
------------------------------------------------

DROP INDEX EMPINST.SURNAME_UDX@

DROP INDEX EMPINST.SURNAME_IDX@

ALTER TABLE EMPINST.SURNAME ADD CONSTRAINT SURNAME_PK PRIMARY KEY (PROF_KEY, PROF_SURNAME)@

CREATE INDEX EMPINST.SURNAME_IDX ON EMPINST.SURNAME (PROF_SURNAME ASC, PROF_KEY ASC)@


------------------------------------------------
-- DDL Statements for table "EMPINST"."GIVEN_NAME"
------------------------------------------------

DROP INDEX EMPINST.GIVEN_NAME_UDX@

DROP INDEX EMPINST.GIVEN_NAME_IDX@

ALTER TABLE EMPINST.GIVEN_NAME ADD CONSTRAINT GIVEN_NAME_PK PRIMARY KEY (PROF_KEY, PROF_GIVENNAME)@

CREATE INDEX EMPINST.GIVEN_NAME_IDX ON EMPINST.GIVEN_NAME (PROF_GIVENNAME ASC, PROF_KEY ASC)@

 		 
------------------------------------------------
-- DDL Statements for table EMPINST.PROFILE_EXTENSIONS
------------------------------------------------

DROP INDEX EMPINST.PROFILE_EXTENSIONS_UDX@

ALTER TABLE EMPINST.PROFILE_EXTENSIONS ADD CONSTRAINT PROFILE_EXT_PK PRIMARY KEY (PROF_KEY, PROF_PROPERTY_ID)@


------------------------------------------------
-- DDL Statements for sequence for Table EMPINST.PROFILE_EXT_DRAFT
------------------------------------------------


DROP INDEX  EMPINST.EXT_DRAFT_PK@

ALTER TABLE EMPINST.PROFILE_EXT_DRAFT ADD CONSTRAINT EXT_DRAFT_PK PRIMARY KEY (PROF_KEY, PROF_UPDATE_SEQUENCE)@



-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 27 WHERE COMPKEY='Profiles'@


COMMIT@
CONNECT RESET@

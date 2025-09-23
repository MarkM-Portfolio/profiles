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
{include.dynaDefsFixUp02.sql}

-- Rebuild

DROP TABLE EMPINST.USER_STATE_LAIDX;

--
-- User state look aside index table
--
CREATE TABLE EMPINST.USER_STATE_LAIDX (
    DEFID    RAW(16) NOT NULL,
	LAIDXLID		RAW(16) NOT NULL,
	USRSTATE		NUMBER(19,0) NOT NULL,
	USRSTATE_UPDATED	TIMESTAMP NOT NULL,
	OBJID			RAW(16) NOT NULL,
	OBJIDSTR		CHAR(36) NOT NULL,
	CONSTRAINT USER_STATE_PK PRIMARY KEY (LAIDXLID)
) TABLESPACE PROFREGTABSPACE;

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
 REFERENCES EMPINST.DYNA_DEFS(DEFID)
 ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_OBJ_FK FOREIGN KEY (OBJID) 
	REFERENCES EMPINST.DYNA_OBJ_REF(OBJID)
	ON DELETE CASCADE INITIALLY DEFERRED DEFERRABLE;
	
	

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 23 WHERE COMPKEY='Profiles';

COMMIT;
QUIT;

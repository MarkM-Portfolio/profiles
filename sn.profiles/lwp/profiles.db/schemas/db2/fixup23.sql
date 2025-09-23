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

{include.dynaDefsFixUp02.sql}

-- Rebuild
DROP TABLE EMPINST.USER_STATE_LAIDX@

--
-- User state look aside index table
--
CREATE TABLE EMPINST.USER_STATE_LAIDX (
 DEFID    CHAR(16) FOR BIT DATA NOT NULL,
 IDXID    CHAR(16) FOR BIT DATA NOT NULL,
 USRSTATE   INTEGER NOT NULL,
 USRSTATE_UPDATED TIMESTAMP NOT NULL,
 OBJID    CHAR(16) FOR BIT DATA NOT NULL,
 OBJIDSTR   CHAR(36) NOT NULL,
 CONSTRAINT PK PRIMARY KEY (IDXID)
) IN USERSPACE4K INDEX IN USERSPACE4K@

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_DEF_FK FOREIGN KEY (DEFID) 
 REFERENCES EMPINST.DYNA_DEFS(DEFID)
 ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@
 
--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_OBJ_FK FOREIGN KEY (OBJID) 
 REFERENCES EMPINST.DYNA_OBJ_REF(OBJID)
 ON DELETE CASCADE ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION@

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 23 WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

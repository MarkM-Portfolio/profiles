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
USE PEOPLEDB
GO

{include.createDynaDefs.sql}

--
-- User state look aside index table
--
CREATE TABLE EMPINST.USER_STATE_LAIDX (
	LAIDXLID			BINARY(16) NOT NULL,
	USRSTATE			INT NOT NULL,
	USRSTATE_UPDATED		DATETIME NOT NULL,
	OBJID				BINARY(16) NOT NULL,
	OBJIDSTR			NCHAR(36) NOT NULL,
	CONSTRAINT USER_STATE_PK PRIMARY KEY (LAIDXLID)
);
GO

--
-- Foriegn key constraint on obj-ref table to clean up on deletion
--
ALTER TABLE EMPINST.USER_STATE_LAIDX ADD CONSTRAINT USRSTATE_OBJ_FK FOREIGN KEY (OBJID) 
	REFERENCES EMPINST.DYNA_OBJ_REF(OBJID)
	ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constrain to one value per user
--
CREATE UNIQUE INDEX USRSTATE_CONS_VALB 
	ON EMPINST.USER_STATE_LAIDX (OBJID ASC);
	
CREATE UNIQUE INDEX USRSTATE_CONS_VALS 
	ON EMPINST.USER_STATE_LAIDX (OBJIDSTR ASC);
GO

--
-- Main index for scanning
--
CREATE INDEX USRSTATE_IDX
	ON EMPINST.USER_STATE_LAIDX (USRSTATE ASC, USRSTATE_UPDATED ASC, OBJID ASC, OBJIDSTR ASC);
GO

-- Update schema versions
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 21 WHERE COMPKEY='Profiles';
GO

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

--
-- Board schema updates
--
{include.msgVector-fixup6.sql}

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 30, RELEASEVER='3.0.1.0' WHERE COMPKEY='Profiles'@

--
-- Missing this index for 3.0 createDB; but not upgrade.  Conditionally drop/re-add ndex
--
--#SET TERMINATOR @
CREATE PROCEDURE EMPINST.QUIET_DROP( IN statement VARCHAR(1000) )
LANGUAGE SQL
BEGIN
   DECLARE SQLSTATE CHAR(5)@
   DECLARE NotThere    CONDITION FOR SQLSTATE '42704'@
   DECLARE NotThereSig CONDITION FOR SQLSTATE '42883'@

   DECLARE EXIT HANDLER FOR NotThere, NotThereSig
      SET SQLSTATE = '     '@

   SET statement = 'DROP ' || statement@
   EXECUTE IMMEDIATE statement@
END@
--#SET TERMINATOR @

CALL EMPINST.QUIET_DROP('INDEX EMPINST.ED_UPDATE_IDX')@

COMMIT@

CREATE INDEX "EMPINST"."ED_UPDATE_IDX" ON "EMPINST"."EMP_DRAFT" 
		("PROF_LAST_UPDATE" DESC, "PROF_KEY" DESC, "PROF_UPDATE_SEQUENCE" DESC)@

COMMIT@

DROP PROCEDURE empinst.quiet_drop@

COMMIT@
CONNECT RESET@

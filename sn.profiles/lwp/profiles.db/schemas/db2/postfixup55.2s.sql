-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2016
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68

CONNECT TO PEOPLEDB@

------
-- PHOTO_GUID table added in postfixup55.1
------
--DECLARE CONTINUE HANDLER FOR SQLSTATE '42704'
--  BEGIN
--  END;

CREATE OR REPLACE PROCEDURE EMPINST.DROP_BACKFILL_PHOTO_GUID ( )
  BEGIN
    DECLARE sp_name VARCHAR(128);
    DECLARE sp_schema VARCHAR(128);
    DECLARE dropcmd VARCHAR(512);
    SET sp_name = '';
    SELECT ROUTINESCHEMA,SPECIFICNAME into sp_schema,sp_name FROM SYSIBM.SYSROUTINES WHERE TRIM(UPPER(ROUTINENAME)) = 'BACKFILL_PHOTO_GUID' AND TRIM(UPPER(ROUTINESCHEMA)) = 'EMPINST' FETCH FIRST 1 ROWS ONLY  ;
    IF ( sp_name is not null and sp_name <> ''  ) THEN
      SET dropcmd = 'DROP SPECIFIC PROCEDURE ' ||  sp_schema || '.' || sp_name ;
      EXECUTE IMMEDIATE dropcmd ;
    END IF;
  END@

CALL EMPINST.DROP_BACKFILL_PHOTO_GUID ( )@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER='55.2', RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------

CONNECT RESET@

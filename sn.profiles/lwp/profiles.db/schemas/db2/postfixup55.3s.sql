CONNECT TO PEOPLEDB@

CREATE OR REPLACE PROCEDURE EMPINST.REVERT_PHOTO ( 
                                                        OUT row_cnt     INTEGER,
                                                        OUT skipped_cnt INTEGER,
                                                        OUT upd_cnt     INTEGER,
                                                        OUT begin_ts    TIMESTAMP,                                                      
                                                        OUT end_ts      TIMESTAMP,
                                                        OUT my_state    CHAR(5),
                                                        OUT last_key    VARCHAR(36) ) 
LANGUAGE SQL MODIFIES SQL DATA 
BEGIN 
DECLARE SQLCODE INTEGER DEFAULT 0 ;
DECLARE SQLSTATE CHAR(5) DEFAULT '00000' ;
DECLARE current_guid VARCHAR(36) ;
DECLARE last_guid VARCHAR(36) ;
DECLARE upd_prof_key VARCHAR(36);
DECLARE upd_home_tk VARCHAR(36);
DECLARE postschemaver VARCHAR(10) ;
DECLARE CONTINUE HANDLER FOR SQLSTATE '23505'
  BEGIN
    set my_state = '23505';
  END;
SET row_cnt = 0 ;
SET skipped_cnt = 0;
SET upd_cnt = 0;
SET begin_ts = CURRENT TIMESTAMP ;
SET current_guid = '0' ;
SET last_guid = '0' ;

-- take no action unless we see that the stroed procedure to fill the PHOTO_GUID table is in place
IF NOT EXISTS ( SELECT SPECIFICNAME FROM SYSIBM.SYSROUTINES WHERE TRIM(UPPER(ROUTINENAME)) = 'BACKFILL_PHOTO_GUID' AND TRIM(UPPER(ROUTINESCHEMA)) = 'EMPINST' ) THEN
  GOTO exit;
END IF;

NEXTROW: LOOP
    SELECT PROF_GUID INTO current_guid FROM EMPINST.PHOTO_GUID WHERE PROF_GUID > last_guid ORDER BY PROF_GUID FETCH FIRST ROW ONLY ;
    IF (SQLCODE = 100) THEN  LEAVE NEXTROW ; END IF ;
    SET last_guid = current_guid ;
    SET row_cnt = row_cnt + 1 ;
    SET my_state = '00000';
    -- attempt to insert the row
    INSERT INTO EMPINST.PHOTO (PROF_KEY, PROF_FILE_TYPE, PROF_UPDATED, PROF_IMAGE, PROF_THUMBNAIL, TENANT_KEY)
      SELECT E.PROF_KEY, PG.PROF_FILE_TYPE, PG.PROF_UPDATED, PG.PROF_IMAGE, PG.PROF_THUMBNAIL, E.H_TENANT_KEY  TENANT_KEY
        FROM EMPINST.EMPLOYEE E, EMPINST.PHOTO_GUID PG
        WHERE E.PROF_GUID = current_guid AND E.PROF_GUID = PG.PROF_GUID ;
        -- AND E.H_TENANT_KEY = E.TENANT_KEY ; causes an issue if a user has a photo only in a visiting org
    -- if the row already exists attempt to update it
    IF (my_state = '23505') THEN
        SET upd_prof_key = '0';
        SET upd_home_tk = '0';
  
        SELECT P.PROF_KEY, PG.H_TENANT_KEY into upd_prof_key, upd_home_tk FROM EMPINST.EMPLOYEE E, EMPINST.PHOTO_GUID PG, EMPINST.PHOTO P 
           WHERE PG.PROF_GUID = current_guid
           AND E.PROF_GUID = PG.PROF_GUID
           AND P.PROF_KEY = E.PROF_KEY
           AND E.TENANT_KEY = PG.H_TENANT_KEY
           AND P.PROF_UPDATED < PG.PROF_UPDATED;

        IF ((upd_prof_key <> '0' AND upd_prof_key IS NOT NULL) AND (upd_home_tk <> '0' AND upd_home_tk IS NOT NULL)) THEN
           SET upd_cnt = upd_cnt + 1 ;
           UPDATE EMPINST.PHOTO AS P SET ( PROF_FILE_TYPE, PROF_UPDATED, PROF_IMAGE, PROF_THUMBNAIL ) = ( 
              SELECT PG.PROF_FILE_TYPE, PG.PROF_UPDATED, PG.PROF_IMAGE, PG.PROF_THUMBNAIL
              FROM EMPINST.PHOTO_GUID PG
              WHERE PG.PROF_GUID = current_guid )
           WHERE P.PROF_KEY = upd_prof_key
           AND  P.TENANT_KEY = upd_home_tk;
          -- if no matching row was found, including the case where the matching row was newer, then skip it 
          IF (SQLCODE = 100) THEN 
            SET skipped_cnt = skipped_cnt + 1;
          END IF;
        ELSE 
          SET skipped_cnt = skipped_cnt + 1;
        END IF;
    END IF;
    COMMIT;
END LOOP NEXTROW ;

exit:
SET end_ts = CURRENT TIMESTAMP ;
END @

------
-- back fill PHOTO_GUID
------

CALL  EMPINST.REVERT_PHOTO( ?, ?, ?, ?, ?, ?, ?)@

COMMIT@

------
-- reorg table after adding content
------

REORG TABLE EMPINST.PHOTO@

COMMIT@

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, POSTSCHEMAVER='55.3', RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles'@

COMMIT@

------
-- Disconnect
------

CONNECT RESET@

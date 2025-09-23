
-- ***************************************************************** 
-- 
-- IBM Confidential 
-- 
-- OCO Source Materials 
-- 
-- Copyright IBM Corp. 2007, 2016     
-- 
-- The source code for this program is not published or otherwise 
-- divested of its trade secrets, irrespective of what has been 
-- deposited with the U.S. Copyright Office. 
-- 
-- ***************************************************************** 

connect to PEOPLEDB@


set integrity for EMPINST.GIVEN_NAME immediate checked@
set integrity for EMPINST.SURNAME immediate checked@
set integrity for EMPINST.PROFILE_EXTENSIONS immediate checked@
set integrity for EMPINST.PROFILE_EXT_DRAFT immediate checked@
set integrity for EMPINST.PEOPLE_TAG immediate checked@
set integrity for EMPINST.DEPARTMENT immediate checked@
set integrity for EMPINST.ORGANIZATION immediate checked@
set integrity for EMPINST.COUNTRY immediate checked@
set integrity for EMPINST.EMP_TYPE immediate checked@
set integrity for EMPINST.EMPLOYEE immediate checked@
set integrity for EMPINST.PHOTO immediate checked@
set integrity for EMPINST.PHOTO_GUID immediate checked@
set integrity for EMPINST.PRONUNCIATION immediate checked@
set integrity for EMPINST.WORKLOC immediate checked@
set integrity for EMPINST.EMP_DRAFT immediate checked@
set integrity for EMPINST.CHG_EMP_DRAFT  immediate checked@
set integrity for EMPINST.PROF_CONNECTIONS immediate checked@
set integrity for EMPINST.SNPROF_SCHEMA immediate checked@
set integrity for EMPINST.PROF_CONSTANTS immediate checked@
set integrity for EMPINST.EVENTLOG immediate checked@
set integrity for EMPINST.PROFILE_LOGIN immediate checked@
set integrity for EMPINST.PROFILE_PREFS immediate checked@
set integrity for EMPINST.PROFILE_LAST_LOGIN immediate checked@
set integrity for EMPINST.USER_PLATFORM_EVENTS immediate checked@
set integrity for EMPINST.TENANT immediate checked@
set integrity for EMPINST.EMP_ROLE_MAP immediate checked@
set integrity for EMPINST.EMP_UPDATE_TIMESTAMP immediate checked@

set integrity for EMPINST.PROFILES_SCHEDULER_TASK immediate checked@
set integrity for EMPINST.PROFILES_SCHEDULER_LMGR immediate checked@
set integrity for EMPINST.PROFILES_SCHEDULER_LMPR immediate checked@
set integrity for EMPINST.PROFILES_SCHEDULER_TREG immediate checked@

COMMIT@

connect reset@

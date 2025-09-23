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

-- 5724-S68                                                          
CONNECT TO PEOPLEDB@


reorg indexes all for table EMPINST.GIVEN_NAME@
reorg indexes all for table EMPINST.SURNAME@
reorg indexes all for table EMPINST.PROFILE_EXTENSIONS@
reorg indexes all for table EMPINST.PROFILE_EXT_DRAFT@
reorg indexes all for table EMPINST.PEOPLE_TAG@
reorg indexes all for table EMPINST.DEPARTMENT@
reorg indexes all for table EMPINST.ORGANIZATION@
reorg indexes all for table EMPINST.COUNTRY@
reorg indexes all for table EMPINST.EMP_TYPE@
reorg indexes all for table EMPINST.EMPLOYEE@
reorg indexes all for table EMPINST.PHOTO@
reorg indexes all for table EMPINST.PHOTO_GUID@
reorg indexes all for table EMPINST.PRONUNCIATION@
reorg indexes all for table EMPINST.WORKLOC@
reorg indexes all for table EMPINST.EMP_DRAFT@
reorg indexes all for table EMPINST.PROF_CONNECTIONS@
reorg indexes all for table EMPINST.EVENTLOG@
reorg indexes all for table EMPINST.PROFILE_LOGIN@
reorg indexes all for table EMPINST.PROFILE_PREFS@
reorg indexes all for table EMPINST.PROFILE_LAST_LOGIN@
reorg indexes all for table EMPINST.TENANT@
reorg indexes all for table EMPINST.EMP_ROLE_MAP@
reorg indexes all for table EMPINST.EMP_UPDATE_TIMESTAMP@

reorg indexes all for table EMPINST.USER_PLATFORM_EVENTS@
reorg indexes all for table EMPINST.PROFILES_SCHEDULER_TASK@
reorg indexes all for table EMPINST.PROFILES_SCHEDULER_LMGR@
reorg indexes all for table EMPINST.PROFILES_SCHEDULER_LMPR@
reorg indexes all for table EMPINST.PROFILES_SCHEDULER_TREG@


reorg table EMPINST.GIVEN_NAME use TEMPSPACE4K@
reorg table EMPINST.SURNAME use TEMPSPACE4K@
reorg table EMPINST.PROFILE_EXTENSIONS INDEX EMPINST.PROFILE_EXTENSIONS_IDX use TEMPSPACE4K@
reorg table EMPINST.PROFILE_EXT_DRAFT INDEX  EMPINST.EXT_DRAFT_PK use TEMPSPACE4K@
reorg table EMPINST.PEOPLE_TAG INDEX EMPINST.PEOPLE_TAG_IDX2 use TEMPSPACE4K@
reorg table EMPINST.DEPARTMENT use TEMPSPACE4K@
reorg table EMPINST.ORGANIZATION use TEMPSPACE4K@
reorg table EMPINST.COUNTRY  use TEMPSPACE4K@
reorg table EMPINST.EMP_TYPE use TEMPSPACE4K@
reorg table EMPINST.EMPLOYEE use TEMPSPACE32K@
reorg table EMPINST.PHOTO use TEMPSPACE4K@ 
reorg table EMPINST.PRONUNCIATION use TEMPSPACE4K@
reorg table EMPINST.WORKLOC use TEMPSPACE4K@
reorg table EMPINST.EMP_DRAFT use TEMPSPACE32K@
reorg table EMPINST.CHG_EMP_DRAFT use TEMPSPACE4K@
reorg table EMPINST.PROF_CONNECTIONS INDEX EMPINST.CONN_INDEX1 use TEMPSPACE4K@
reorg table EMPINST.PROF_CONSTANTS use TEMPSPACE4K@
reorg table EMPINST.SNPROF_SCHEMA use TEMPSPACE4K@
reorg table EMPINST.EVENTLOG use TEMPSPACE4K@
reorg table EMPINST.PROFILE_LOGIN use TEMPSPACE4K@
reorg table EMPINST.PROFILE_PREFS use TEMPSPACE4K@
reorg table EMPINST.PROFILE_LAST_LOGIN use TEMPSPACE4K@
reorg table EMPINST.TENANT use TEMPSPACE32K@
reorg table EMPINST.EMP_ROLE_MAP use TEMPSPACE4K@
reorg table EMPINST.EMP_UPDATE_TIMESTAMP use TEMPSPACE4K@

reorg table EMPINST.USER_PLATFORM_EVENTS use TEMPSPACE4K@
reorg table EMPINST.PROFILES_SCHEDULER_TASK use TEMPSPACE4K@
reorg table EMPINST.PROFILES_SCHEDULER_LMGR use TEMPSPACE4K@
reorg table EMPINST.PROFILES_SCHEDULER_LMPR use TEMPSPACE4K@
reorg table EMPINST.PROFILES_SCHEDULER_TREG use TEMPSPACE4K@

COMMIT@

CONNECT RESET@

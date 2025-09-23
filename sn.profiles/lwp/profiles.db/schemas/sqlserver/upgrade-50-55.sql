-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2015                                          
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68
                                                      
USE PEOPLEDB;

GO

------
-- default tenant (fixup47)
------

UPDATE EMPINST.TENANT set TENANT_KEY ='a', TENANT_EXID = 'a' where TENANT_KEY ='00000000-0000-0000-0000-040508202233'; 

GO

------
-- idhash (fixup48)
------

ALTER TABLE EMPINST.EMPLOYEE ADD PROF_IDHASH NVARCHAR(256)  DEFAULT '?' NOT NULL;

CREATE INDEX IDHASH_UDX ON EMPINST.EMPLOYEE (PROF_IDHASH ASC, TENANT_KEY);

GO

-------
-- tenant exid index (fixup49)
-------

CREATE UNIQUE INDEX TENANT_EXID_UDX ON EMPINST.TENANT (TENANT_EXID ASC);

GO

------
-- idhash migration (fixup51)
-- if customer wants to backfill (the not public) prof_idhash, run a java update.
------

------
-- add home org/tenant key (fixup 52)
------

ALTER TABLE EMPINST.EMPLOYEE ADD H_TENANT_KEY NVARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-040508202233';

GO

------
-- no op on prem (fixup 53)
------

------
-- tdi timestamp table (fixup 54)
------

CREATE TABLE EMPINST.EMP_UPDATE_TIMESTAMP (
	PROF_KEY					NVARCHAR(36) NOT NULL, 
	TENANT_KEY					NVARCHAR(36) DEFAULT '00000000-0000-0000-0000-040508202233' NOT NULL,
	LDAP_MOD_TIMESTAMP_SOURCE	NVARCHAR(36) NOT NULL,
	LDAP_MOD_TIMESTAMP			NVARCHAR(128) DEFAULT '0' NOT NULL,
	MARK						NVARCHAR(1) DEFAULT '0' NOT NULL
);

GO

ALTER TABLE EMPINST.EMP_UPDATE_TIMESTAMP ADD CONSTRAINT TS_PK PRIMARY KEY (PROF_KEY, TENANT_KEY, LDAP_MOD_TIMESTAMP_SOURCE);

GO

------
--  add pre/post schema version info (fixup55)
------

ALTER TABLE EMPINST.SNPROF_SCHEMA ADD PRESCHEMAVER NVARCHAR(10) DEFAULT '0' NOT NULL;
ALTER TABLE EMPINST.SNPROF_SCHEMA ADD POSTSCHEMAVER NVARCHAR(10) DEFAULT '0' NOT NULL;

GO

------
-- update schema
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER=55, RELEASEVER='5.5.0.0' WHERE COMPKEY='Profiles';

GO

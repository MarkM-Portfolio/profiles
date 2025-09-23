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


------------------------------------------------
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS"
------------------------------------------------

CREATE TABLE "EMPINST"."USER_PLATFORM_EVENTS"  (
	"EVENT_KEY" BIGINT NOT NULL, 
	"EVENT_TYPE" VARCHAR(36) NOT NULL,
	"PAYLOAD" CLOB(100K) LOGGED COMPACT,
	"CREATED" TIMESTAMP NOT NULL,
	CONSTRAINT "PLATFORM_EVENTS_PK" PRIMARY KEY ("EVENT_KEY") 
) IN USERSPACE4K INDEX IN USERSPACE4K@


-------------------------------------------------------------------
-- START ADDING SCHEDULER TABLES
-------------------------------------------------------------------

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_TASK" (
	TASKID BIGINT NOT NULL,
	VERSION VARCHAR(5) NOT NULL,
	ROW_VERSION INTEGER NOT NULL,
	TASKTYPE INTEGER NOT NULL,
	TASKSUSPENDED SMALLINT NOT NULL,
	CANCELLED SMALLINT NOT NULL,
	NEXTFIRETIME BIGINT NOT NULL,
	STARTBYINTERVAL VARCHAR(254),
	STARTBYTIME BIGINT,
	VALIDFROMTIME BIGINT,
	VALIDTOTIME BIGINT,
	REPEATINTERVAL VARCHAR(254),
	MAXREPEATS INTEGER NOT NULL,
	REPEATSLEFT INTEGER NOT NULL,
	TASKINFO BLOB(102400) LOGGED NOT COMPACT,
	NAME VARCHAR(254) NOT NULL,
	AUTOPURGE INTEGER NOT NULL,
	FAILUREACTION INTEGER,
	MAXATTEMPTS INTEGER,
	QOS INTEGER,
	PARTITIONID INTEGER,
	OWNERTOKEN VARCHAR(200) NOT NULL,
	CREATETIME BIGINT NOT NULL
) 
IN USERSPACE4K INDEX IN USERSPACE4K@

ALTER TABLE "EMPINST".PROFILES_SCHEDULER_TASK 
	ADD PRIMARY KEY (TASKID)@

CREATE INDEX "EMPINST".PROFILES_SCHEDULER_TASK_IDX1 
	ON "EMPINST"."PROFILES_SCHEDULER_TASK" (TASKID, OWNERTOKEN)@

CREATE INDEX "EMPINST".PROFILES_SCHEDULER_TASK_IDX2 
	ON "EMPINST"."PROFILES_SCHEDULER_TASK" (NEXTFIRETIME ASC, REPEATSLEFT, PARTITIONID) CLUSTER@

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_TREG" (
	REGKEY VARCHAR(254) NOT NULL,
	REGVALUE VARCHAR(254) 
) 
IN USERSPACE4K INDEX IN USERSPACE4K@

ALTER TABLE "EMPINST".PROFILES_SCHEDULER_TREG 
	ADD PRIMARY KEY (REGKEY)@

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_LMGR" (
	LEASENAME VARCHAR(254) NOT NULL,
	LEASEOWNER VARCHAR(254) NOT NULL,
	LEASE_EXPIRE_TIME  BIGINT,
	DISABLED VARCHAR(5)
)
IN USERSPACE4K INDEX IN USERSPACE4K@

ALTER TABLE "EMPINST"."PROFILES_SCHEDULER_LMGR"
	ADD PRIMARY KEY (LEASENAME)@

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_LMPR" (
	LEASENAME VARCHAR(254) NOT NULL,
	NAME VARCHAR(254) NOT NULL,
	VALUE VARCHAR(254) NOT NULL
)
IN USERSPACE4K INDEX IN USERSPACE4K@

CREATE INDEX "EMPINST"."PROFILES_SCHEDULER_LMPR_IDX1 "
	ON "EMPINST"."PROFILES_SCHEDULER_LMPR" (LEASENAME, NAME)@
	
-------------------------------------------------------------------
-- END ADDING SCHEDULER TABLES
-------------------------------------------------------------------

-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 19, RELEASEVER='3.0.0.0' WHERE COMPKEY='Profiles'@

COMMIT@
CONNECT RESET@

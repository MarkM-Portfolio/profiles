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
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS"
------------------------------------------------

CREATE TABLE "EMPINST"."USER_PLATFORM_EVENTS"  (
	"EVENT_KEY" NUMBER(19, 0) NOT NULL, 
	"EVENT_TYPE" VARCHAR2(36) NOT NULL,
	"PAYLOAD" CLOB,
	"CREATED" TIMESTAMP NOT NULL,
	PRIMARY KEY ("EVENT_KEY") 
) TABLESPACE PROFREGTABSPACE;

-------------------------------------------------------------------
-- START ADDING SCHEDULER TABLES
-------------------------------------------------------------------

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_TASK" (
	TASKID NUMBER(19, 0) NOT NULL,
	VERSION VARCHAR2(5) NOT NULL,
	ROW_VERSION NUMBER(10, 0) NOT NULL,
	TASKTYPE NUMBER(10, 0) NOT NULL,
	TASKSUSPENDED NUMBER(5, 0) NOT NULL,
	CANCELLED NUMBER(5, 0) NOT NULL,
	NEXTFIRETIME NUMBER(19, 0) NOT NULL,
	STARTBYINTERVAL VARCHAR2(254),
	STARTBYTIME NUMBER(19, 0),
	VALIDFROMTIME NUMBER(19, 0),
	VALIDTOTIME NUMBER(19, 0),
	REPEATINTERVAL VARCHAR2(254),
	MAXREPEATS NUMBER(10, 0) NOT NULL,
	REPEATSLEFT NUMBER(10, 0) NOT NULL,
	TASKINFO BLOB,
	NAME VARCHAR2(254) NOT NULL,
	AUTOPURGE NUMBER(10, 0) NOT NULL,
	FAILUREACTION NUMBER(10, 0),
	MAXATTEMPTS NUMBER(10, 0),
	QOS NUMBER(10, 0),
	PARTITIONID NUMBER(10, 0),
	OWNERTOKEN VARCHAR2(200) NOT NULL,
	CREATETIME NUMBER(19, 0) NOT NULL,
	PRIMARY KEY (TASKID)
) 
TABLESPACE PROFREGTABSPACE;

CREATE INDEX "EMPINST".PROFILES_SCHEDULER_TASK_IDX1 
	ON "EMPINST"."PROFILES_SCHEDULER_TASK" ("TASKID", "OWNERTOKEN") TABLESPACE PROFINDEXTABSPACE;

CREATE INDEX "EMPINST".PROFILES_SCHEDULER_TASK_IDX2 
	ON "EMPINST"."PROFILES_SCHEDULER_TASK" ("NEXTFIRETIME" ASC, "REPEATSLEFT", "PARTITIONID") TABLESPACE PROFINDEXTABSPACE;

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_TREG" (
	REGKEY VARCHAR2(254) NOT NULL,
	REGVALUE VARCHAR2(254),
	PRIMARY KEY (REGKEY)
) 
TABLESPACE PROFREGTABSPACE;

CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_LMGR" (
	LEASENAME VARCHAR2(254) NOT NULL,
	LEASEOWNER VARCHAR2(254) NOT NULL,
	LEASE_EXPIRE_TIME  NUMBER(19, 0),
	DISABLED VARCHAR2(5),
	PRIMARY KEY (LEASENAME)
)
TABLESPACE PROFREGTABSPACE;


CREATE TABLE "EMPINST"."PROFILES_SCHEDULER_LMPR" (
	LEASENAME VARCHAR2(254) NOT NULL,
	NAME VARCHAR2(254) NOT NULL,
	VALUE VARCHAR2(254) NOT NULL
)
TABLESPACE PROFREGTABSPACE;

CREATE INDEX "EMPINST"."PROFILES_SCHEDULER_LMPR_IDX1"
	ON "EMPINST"."PROFILES_SCHEDULER_LMPR" ("LEASENAME", "NAME") TABLESPACE PROFINDEXTABSPACE;
	
-------------------------------------------------------------------
-- END ADDING SCHEDULER TABLES
-------------------------------------------------------------------


------------------------------------------------
-- Update schema version
------------------------------------------------
UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 19, RELEASEVER='3.0.0.0' WHERE COMPKEY='Profiles';

COMMIT;

QUIT;

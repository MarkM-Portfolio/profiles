-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2001, 2010                                    
--                                                                   
-- The source code for this program is not published or otherwise    
-- divested of its trade secrets, irrespective of what has been      
-- deposited with the U.S. Copyright Office.                         
--                                                                   
-- ***************************************************************** 

-- 5724-S68                                                          
USE PEOPLEDB;
GO

------------------------------------------------
-- DDL Statements for table "EMPINST"."USER_PLATFORM_EVENTS"
------------------------------------------------

CREATE TABLE EMPINST.USER_PLATFORM_EVENTS (
	EVENT_KEY BIGINT NOT NULL, 
	EVENT_TYPE NVARCHAR(36) NOT NULL,
	PAYLOAD NVARCHAR(max),
	CREATED DATETIME NOT NULL,
	CONSTRAINT PLATFORM_EVENTS_PK PRIMARY KEY (EVENT_KEY) 
) ;
GO


-------------------------------------------------------------------
-- START ADDING SCHEDULER TABLES
-------------------------------------------------------------------

CREATE TABLE EMPINST.PROFILES_SCHEDULER_TASK (
	TASKID BIGINT NOT NULL,
	VERSION NVARCHAR(5) NOT NULL,
	ROW_VERSION INT NOT NULL,
	TASKTYPE INT NOT NULL,
	TASKSUSPENDED TINYINT NOT NULL,
	CANCELLED TINYINT NOT NULL,
	NEXTFIRETIME BIGINT NOT NULL,
	STARTBYINTERVAL NVARCHAR(254),
	STARTBYTIME BIGINT,
	VALIDFROMTIME BIGINT,
	VALIDTOTIME BIGINT,
	REPEATINTERVAL NVARCHAR(254),
	MAXREPEATS INT NOT NULL,
	REPEATSLEFT INT NOT NULL,
	TASKINFO IMAGE NULL,
	NAME NVARCHAR(254) NOT NULL,
	AUTOPURGE INT NOT NULL,
	FAILUREACTION INT,
	MAXATTEMPTS INT,
	QOS INT,
	PARTITIONID INT,
	OWNERTOKEN NVARCHAR(200) NOT NULL,
	CREATETIME BIGINT NOT NULL
) ;
GO

ALTER TABLE EMPINST.PROFILES_SCHEDULER_TASK WITH NOCHECK 
	ADD CONSTRAINT PROFILES_SCHEDULER_TASK_PK PRIMARY KEY  NONCLUSTERED ( TASKID );
GO

CREATE INDEX PROFILES_SCHEDULER_TASK_IDX1 
	ON EMPINST.PROFILES_SCHEDULER_TASK (TASKID, OWNERTOKEN) ;
GO

CREATE INDEX PROFILES_SCHEDULER_TASK_IDX2 
	ON EMPINST.PROFILES_SCHEDULER_TASK (NEXTFIRETIME ASC, REPEATSLEFT, PARTITIONID) ;
GO

CREATE TABLE EMPINST.PROFILES_SCHEDULER_TREG (
	REGKEY NVARCHAR(254) NOT NULL,
	REGVALUE NVARCHAR(254),
	PRIMARY KEY (REGKEY)
) ;
GO


CREATE TABLE EMPINST.PROFILES_SCHEDULER_LMGR (
	LEASENAME NVARCHAR(254) NOT NULL,
	LEASEOWNER NVARCHAR(254) NOT NULL,
	LEASE_EXPIRE_TIME  BIGINT,
	DISABLED NVARCHAR(5)
) ;
GO

CREATE TABLE EMPINST.PROFILES_SCHEDULER_LMPR (
	LEASENAME NVARCHAR(224) NOT NULL,
	NAME NVARCHAR(224) NOT NULL,
	VALUE NVARCHAR(224) NOT NULL
) ;
GO

CREATE INDEX PROFILES_SCHEDULER_LMPR_IDX1
	ON EMPINST.PROFILES_SCHEDULER_LMPR (LEASENAME, NAME) ;
GO
	
-------------------------------------------------------------------
-- END ADDING SCHEDULER TABLES
-------------------------------------------------------------------


-- Update schema versions

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 19, RELEASEVER='3.0.0.0' WHERE COMPKEY='Profiles';
GO

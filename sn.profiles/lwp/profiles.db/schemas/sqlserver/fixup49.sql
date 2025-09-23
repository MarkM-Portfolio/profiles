-- ***************************************************************** 
--                                                                   
-- IBM Confidential                                                  
--                                                                   
-- OCO Source Materials                                              
--                                                                   
-- Copyright IBM Corp. 2014, 2015
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
-- add TENANT TENANT_EXID_IDX
------

IF EXISTS(SELECT * FROM sys.indexes WHERE object_id = object_id('EMPINST.TENANT') AND NAME ='TENANT_EXID_UDX')
  DROP INDEX TENANT_EXID_UDX ON EMPINST.TENANT ;
GO
CREATE UNIQUE INDEX TENANT_EXID_UDX ON EMPINST.TENANT (TENANT_EXID ASC);
GO

------
-- Update schema version
------

UPDATE EMPINST.SNPROF_SCHEMA SET DBSCHEMAVER= 49, RELEASEVER='5.0.0.0' WHERE COMPKEY='Profiles';
GO

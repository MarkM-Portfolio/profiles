# *****************************************************************
#                                                             
# IBM Confidential                                            
#                                                             
# OCO Source Materials                                        
#                                                             
# Copyright IBM Corp. 2007, 2016                              
#                                                             
# The source code for this program is not published or otherwise    
# divested of its trade secrets, irrespective of what has been
# deposited with the U.S. Copyright Office.                   
#                                                             
# *****************************************************************

#-----------------------------------------------------------------
# profilesAdmin.py
#-----------------------------------------------------------------
#
#  The purpose of this script is to provide basic configuration file management
#  and application administration functions for LotusConnections profiles
import sys, java
import os
from java.lang import System
from java.util import HashMap
from java.util import HashSet
from java.util import ArrayList
from org.python.core import PyList
import exceptions
from com.ibm.ws.scripting import ScriptingException
lineSeparator = java.lang.System.getProperty('line.separator')
initializeOk = 1

from java.lang import String

#  Check for global tenant
try:
    orgIdGlobal
except NameError:
    orgIdGlobal = "default"


#  Check for do not ask for service/node
try:
    batchMode
except NameError:
    batchMode = 0

try:
    serviceNodeNameProfiles
except NameError:
    serviceNodeNameProfiles = None

if (serviceNodeNameProfiles != None):
	bAskForNodeProfiles = 0
else:
	bAskForNodeProfiles = not batchMode

# profiles_config_propkeys
# UR prop names resolove to node attributes or node values through their associated XPath query
# propkeys =
# UI prop_name, [ [xpath_str to resolve prop node], [attribute_name OR None if prop is node value] ]
# last list item indicates supported operations on attribute [1,1] => supports set and get
profiles_config_propkeys = {
"fullReportsToChainCache.enabled": ["//tns:caches/tns:fullReportsToChainCache/tns:enabled", None,[1,1] ],
"fullReportsToChainCache.size": ["//tns:caches/tns:fullReportsToChainCache/tns:size", None,[1,1] ],
"fullReportsToChainCache.refreshTime": ["//tns:caches/tns:fullReportsToChainCache/tns:refreshTime", None,[1,1] ],
"fullReportsToChainCache.refreshInterval": ["//tns:caches/tns:fullReportsToChainCache/tns:refreshInterval", None,[1,1] ],
"fullReportsToChainCache.startDelay": ["//tns:caches/tns:fullReportsToChainCache/tns:startDelay", None,[1,1] ],
"fullReportsToChainCache.ceouid": ["//tns:caches/tns:fullReportsToChainCache/tns:ceouid", None,[1,1] ],
"organizationalStructure.enabled": ["//tns:dataAccess/tns:organizationalStructureEnabled", "enabled",[1,1] ],
"nameOrdering.enabled": ["//tns:dataAccess/tns:nameOrdering","enabled",[1,1] ],
"search.maxRowsToReturn": ["//tns:dataAccess/tns:search/tns:maxRowsToReturn", None,[1,1] ],
"search.pageSize": ["//tns:dataAccess/tns:search/tns:pageSize", None,[1,1] ],
"sametimeAwareness.enabled": ["//tns:sametimeAwareness","enabled",[1,1] ],
"javelinGWMailSearch.enabled": ["//tns:javelinGWMailSearch","enabled",[1,1] ],
"activeContentFilter.enabled": ["//tns:acf","enabled",[1,1] ]
}

def reorderString(inputString):
        # Print out the bean that its connecting to
        output = inputString.substring(0, inputString.indexOf(":")+1)
        #Splitting the string to re-order it for printing
        list = {}
        for item in (inputString.substring(inputString.indexOf(":")+1)).split(','):
                key, value = item.split('=')
                list[key] = value

        for item in ["name", "type", "cell", "node"]:
                output += item + "=" + list[item] + ","
        output += "process=" + list["process"]

        return output.replace("ProfilesAdmin", "ProfilesAdminService")

def printIfNotNull(inputString):
        # https://docs.python.org/2/library/stdtypes.html#truth-value-testing
        if inputString:
                print inputString

#--------------------------------------------------------------
# setOrgId("id of org")
#
#
#--------------------------------------------------------------
def setOrgId(orgIdArg):
		global orgIdGlobal
		#print "orgIdGlobal - set 1"
		#print orgIdGlobal
		orgIdGlobal = orgIdArg
		#print "orgIdGlobal - set 2"
		#print orgIdGlobal
		return "Org id: " + orgIdGlobal

gProfilesArgs = None   #Global configuration arguments -- workingDirectory, cellName, nodeName, etc.

class ProfilesConfigArgs:
	def __init__(self, workingDirectory, cellName, nodeName = None, serverName = None):
		self.workingDirectory = workingDirectory
		assert (workingDirectory), "Missing required argument: workingDirectory must be specified"
		self.cellName = cellName
		assert (cellName), "Missing required argument: cellName must be specified"
		self.nodeName = nodeName
		self.serverName = serverName

	def getRepositoryPath(self):
		#<cell_name>[/nodes/<node_name>[/servers/<server_name>]]
		retVal = self.cellName

		if(self.nodeName and len(self.nodeName) > 0):
			retVal = retVal + "/nodes/" + self.nodeName

		if(self.serverName and len(self.serverName) > 0):
			retVal = retVal + "/servers/" + self.serverName

		return retVal


	def print(self):
		print "\tworkingDirectory:", self.workingDirectory
		print "\tcellName:", self.cellName
		print "\tnodeName:", self.nodeName
		print "\tserverName:", self.serverName


#-----------------------------------------------------------------
# ProfilesConfigurationService  Class
#-----------------------------------------------------------------
#
#
#
#
#
#-----------------------------------------------------------------
class ProfilesConfigurationService:

	def __init__(self):
		self.dynamicPropsAdded = 0

	def set_config_args_from_list(self, args):
		wd = None; cn = None; nn = None; sn = None

		#assert len(args) >= 2, "Missing required arguments: working_directory and cellName must be specified"
		idx = 0
		for s in args:
			if (idx == 0):
				wd = s
				exists = File(wd).exists()
				if(not exists):
					raise "Working directory does not exist", wd
			elif (idx == 1):
				cn = s
			elif (idx == 2):
				nn = s
			elif (idx == 3):
				sn = s
			idx = idx + 1

		global gProfilesArgs
		gProfilesArgs = ProfilesConfigArgs(wd, cn, nn, sn)



	def set_config_args(self, workingDirectory, cellName, nodeName = None, serverName = None):
		global gProfilesArgs
		gProfilesArgs = ProfilesConfigArgs(workingDirectory, cellName, nodeName, serverName)


	def validate_args(self, args):
		global gProfilesArgs
		if ( ( (not args) or len(args) < 2)  and (gProfilesArgs != None)):
			print "Using configuration arguments :"; gProfilesArgs.print()
		else:
			self.set_config_args_from_list(args)


	#--------------------------------------------------------------
	# checkOutConfig
	#
	# Arguments
	#     checkOutConfig(self, working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def checkOutConfig(self, *args):

	    try:
	      self.validate_args(args)
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-config.xml", gProfilesArgs.workingDirectory + "/profiles-config.xml")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-config.xsd", gProfilesArgs.workingDirectory + "/profiles-config.xsd")

	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml")
      	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-policy.xsd", gProfilesArgs.workingDirectory + "/profiles-policy.xsd")

	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-types.xml", gProfilesArgs.workingDirectory + "/profiles-types.xml")
      	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-types.xsd", gProfilesArgs.workingDirectory + "/profiles-types.xsd")

	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml")
      	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/widgets-config.xsd", gProfilesArgs.workingDirectory + "/widgets-config.xsd")

	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/businessCardInfo.ftl", gProfilesArgs.workingDirectory + "/businessCardInfo.ftl")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/commonUtil.ftl", gProfilesArgs.workingDirectory + "/commonUtil.ftl")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/profileDetails.ftl", gProfilesArgs.workingDirectory + "/profileDetails.ftl")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/profileEdit.ftl", gProfilesArgs.workingDirectory + "/profileEdit.ftl")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/searchResults.ftl", gProfilesArgs.workingDirectory + "/searchResults.ftl")

	      print "Profiles configuration files successfully checked out"
	      #if (not self.dynamicPropsAdded):
	      #	print "need to add dynamic props"
	    except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# check_out_connections_proxy_config
	#
	# Arguments
	#     check_out_connections_proxy_config(working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def checkOutProxyConfig(self, *args):
	    try:
	      self.validate_args(args)
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/proxy-profiles-config.tpl", gProfilesArgs.workingDirectory + "/proxy-profiles-config.tpl")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/proxy-config.xsd", gProfilesArgs.workingDirectory + "/proxy-config.xsd")
	      print "Profiles proxy configuration file successfully checked out"
	    except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# check_out_widget_config
	#
	# Arguments
	#     check_out_connections_widget_config(working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def checkOutWidgetConfig(self, *args):
	    try:
	      self.validate_args(args)
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/widgets-config.xsd", gProfilesArgs.workingDirectory + "/widgets-config.xsd")
	      print "Profiles widget configuration file successfully checked out"
	    except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# check_out_policy_config
	#
	# Arguments
	#     check_out_profiles_policy(working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def checkOutPolicyConfig(self, *args):
	    try:
	      self.validate_args(args)
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml")
	      obj = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-policy.xsd", gProfilesArgs.workingDirectory + "/profiles-policy.xsd")
	      print "Profiles policy configuration file successfully checked out"
	    except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# checkInConfig
	#
	# Arguments
	#     checkInConfig(self, working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def  checkInConfig(self, *args):

	   try:
	      self.validate_args(args)
	      print "Validating profiles-config.xml"
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/profiles-config.xml")
	      validator.validateDoc()

	      print "Validating profiles-policy.xml"
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/profiles-policy.xml")
	      validator.validateDoc()

	      print "Validating profiles-types.xml"
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/profiles-types.xml")
	      validator.validateDoc()

	      print "Validating widgets-config.xml"
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/widgets-config.xml")
	      validator.validateDoc()

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-config.xml", gProfilesArgs.workingDirectory + "/profiles-config.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-config.xml", gProfilesArgs.workingDirectory + "/profiles-config.xml", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-types.xml", gProfilesArgs.workingDirectory + "/profiles-types.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles-types.xml", gProfilesArgs.workingDirectory + "/profiles-types.xml", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles/templates/businessCardInfo.ftl", gProfilesArgs.workingDirectory + "/businessCardInfo.ftl_ORIG")
      	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/businessCardInfo.ftl", gProfilesArgs.workingDirectory + "/businessCardInfo.ftl", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles/templates/commonUtil.ftl", gProfilesArgs.workingDirectory + "/commonUtil.ftl_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/commonUtil.ftl", gProfilesArgs.workingDirectory + "/commonUtil.ftl", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles/templates/profileDetails.ftl", gProfilesArgs.workingDirectory + "/profileDetails.ftl_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/profileDetails.ftl", gProfilesArgs.workingDirectory + "/profileDetails.ftl", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles/templates/profileEdit.ftl", gProfilesArgs.workingDirectory + "/profileEdit.ftl_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/profileEdit.ftl", gProfilesArgs.workingDirectory + "/profileEdit.ftl", digest)

	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles/templates/searchResults.ftl", gProfilesArgs.workingDirectory + "/searchResults.ftl_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.cellName + "/LotusConnections-config/profiles/templates/searchResults.ftl", gProfilesArgs.workingDirectory + "/searchResults.ftl", digest)

	      print "Profiles configuration files successfully checked in"
	   except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# check_in_connections_proxy_config
	#
	# Arguments
	#     check_out_connections_config(working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def  checkInProxyConfig(self, *args):

	   try:
	      self.validate_args(args)
	      configDoc = "cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/proxy-profiles-config.tpl"
	      newDoc = gProfilesArgs.workingDirectory + "/proxy-profiles-config.tpl"
	      validator=ProfConfigFileValidator(newDoc)
	      validator.validateDoc()
	      if (AdminConfig.existsDocument(configDoc)):
	        digest = AdminConfig.extract(configDoc, gProfilesArgs.workingDirectory + "/proxy-profiles-config.xml_ORIG")
	        AdminConfig.checkin(configDoc, newDoc, digest)
	      else:
	        AdminConfig.createDocument(configDoc, newDoc)
	      print "Profiles proxy configuration file successfully checked in"
	   except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# checkInWidgetConfig
	#
	# Arguments
	#     checkInWidgetConfig(self, working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def  checkInWidgetConfig(self, *args):

	   try:
	      self.validate_args(args)
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/widgets-config.xml")
	      validator.validateDoc()
	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/widgets-config.xml", gProfilesArgs.workingDirectory + "/widgets-config.xml", digest)
	      print "Widgets configuration file successfully checked in"
	   except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# checkInPolicyConfig
	#
	# Arguments
	#     checkInPolicyConfig(self, working_dir, cellName, nodeName = None , serverName = None)
	#--------------------------------------------------------------
	def  checkInPolicyConfig(self, *args):

	   try:
	      self.validate_args(args)
	      validator=ProfConfigFileValidator(gProfilesArgs.workingDirectory + "/profiles-policy.xml")
	      validator.validateDoc()
	      digest = AdminConfig.extract("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml_ORIG")
	      AdminConfig.checkin("cells/" + gProfilesArgs.getRepositoryPath() + "/LotusConnections-config/profiles-policy.xml", gProfilesArgs.workingDirectory + "/profiles-policy.xml", digest)
	      print "Profiles policy configuration file successfully checked in"
	   except:
	       	c, i, tb = sys.exc_info()
		print "Exception -", c, i

	#--------------------------------------------------------------
	# updateConfig
	#
	# Arguments
	#     updateConfig(self, key, value)
	#--------------------------------------------------------------
	def updateConfig(self, key, value):

	   assert(gProfilesArgs), "\n\t Configuration arguments(workingDirectoy, cellName, etc.) must be set \
	   	\n either by \n\t checkOutConfig(workingDirectory, cellName, nodeName) \
	   	\n or by \n\t set_config_args(workingDirectory, cellName, nodeName)"

	   assert (profiles_config_propkeys[key][2][0]), "Error -- " + "Update not supported for " + key
	   filename = gProfilesArgs.workingDirectory + "/profiles-config.xml"
	   XPathStr = profiles_config_propkeys[key][0]
	   attName = profiles_config_propkeys[key][1]

	   updater = ProfConfigFileReaderUpdater(filename)
	   updater.debugEnabled = 1   #in jython boolean val's are either 1 or 0

	   if(attName):
	   	updater.updateNodeProperty(XPathStr, attName, value)
	   else:
	   	updater.updateNodeValue(XPathStr, value)

	   #save changed file for validation
	   tmpName = filename[:filename.find(".xml")] + "_CHANGED_" + ".xml"
	   updater.fileName = tmpName
	   updater.saveDoc()


	   validator=ProfConfigFileValidator(tmpName)
	   try:
	 	validator.validateDoc()
		updater.fileName = filename
		updater.saveDoc()
	   except:
	     	#validation exceptions logged in validate
	     	print ""


	#--------------------------------------------------------------
	# showConfig
	#
	# Arguments
	#     showConfig(self, key, value)
	#--------------------------------------------------------------
	def showConfig(self):
		assert(gProfilesArgs), "\n\t Configuration arguments(workingDirectoy, cellName, etc.) must be set \
	   	\n either by \n\t checkOutConfig(workingDirectory, cellName, nodeName) \
	   	\n or by \n\t set_config_args(workingDirectory, cellName, nodeName)"

		filename = gProfilesArgs.workingDirectory + "/profiles-config.xml"
		reader = ProfConfigFileReaderUpdater(filename)

		global profiles_config_propkeys

		print "\n profiles configuration properties:"
		sorted_keys = profiles_config_propkeys.keys()
		sorted_keys.sort()
		for s in sorted_keys:
			#JK DEBUG
			#print s
			XPathStr = profiles_config_propkeys[s][0]
			attName = profiles_config_propkeys[s][1]
			if(attName):
				print "\t" + s + " = " + reader.getNodeProperty(XPathStr, attName)
			else:
				print "\t" + s + " = " + reader.getNodeValue(XPathStr)

		print "\n"




# return a hashmap containing the value of the PyDictionary. Only handle string keys - string and list values
# (PyDictionary implements Map interface from 2.5, but we're stuck with 2.1...)
def pyDictionaryToMap(dictionary):
	resultMap = HashMap()

	for key in dictionary.keys():
		value = dictionary[key]

		# handle list - there is probably a more python way to do this...
		if isinstance(value, PyList):
			javaList = ArrayList()
			for v in value:
				javaList.add(v)

			value = javaList

		resultMap.put(key, value)

	return resultMap

#-----------------------------------------------------------------
# ProfilesServiceMBean  Class
#-----------------------------------------------------------------
#
#
#
#
#
#-----------------------------------------------------------------
class ProfilesServiceMBean:

	def __init__(self):
		self.gMBeanName = None
		self.gCellName = None
		self.gNodeName = None
		self.gProcessName = None
		self.beanNameStr = None
		self.beanNameStrBeg = None
		self.beanNameStrEnd = None
		self.objNameString = None

	#--------------------------------------------------------------
	# showOrgId
	#
	#
	#--------------------------------------------------------------
	def showOrgId(self):
			#global orgIdGlobal
			return 	orgIdGlobal

	#--------------------------------------------------------------
	# findDistinctProfileTypeReferences
	#
	#
	#--------------------------------------------------------------
	def findDistinctProfileTypeReferences(self, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String"]
			parms=[orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(), "findDistinctProfileTypeReferences", parms, sig)
			print "findDistinctProfileTypeReferences returned:", rc
			return rc

 	#--------------------------------------------------------------
	# findUndefinedProfileTypeReferences
	#
	#
	#--------------------------------------------------------------
	def findUndefinedProfileTypeReferences(self, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String"]
			parms=[orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(), "findUndefinedProfileTypeReferences", parms, sig)
			print "findUndefinedProfileTypeReferences processed:", rc
			return rc

	#--------------------------------------------------------------
	# disableFullReportsToCache
	#
	#
	#--------------------------------------------------------------
	def disableFullReportsToCache(self):
			rc = AdminControl.invoke(str(self.getMBeanName()),"disableFullReportsToCache")
			print "disableFullReportsToCache request processed"

	#--------------------------------------------------------------
	# enableFullReportsToCache
	#
	#
	#--------------------------------------------------------------
	def enableFullReportsToCache(self, startDelay, interval, schedTime):
			sig = ["int", "int", "java.lang.String"]
			parms=[startDelay, interval, schedTime]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"enableFullReportsToCache",parms, sig)
			print "enableFullReportsToCache request processed"


	#--------------------------------------------------------------
	# reloadFullReportsToCache
	#
	#
	#--------------------------------------------------------------
	def reloadFullReportsToCache(self):
			rc = AdminControl.invoke(str(self.getMBeanName()),"reloadFullReportsToCache")
			print "reloadFullReportsToCache request processed"

	#--------------------------------------------------------------
	# updateDescription
	#
	#
	#--------------------------------------------------------------
	def updateDescription(self, email, description, orgId="_default_"):
			# if orgId is specified on command line, i.e., not equal to _default_, use command line value
			# else use global value, which is set to "default" if not specified
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[email, description, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateDescription",parms, sig)
			print "updateDescription request processed"

	#--------------------------------------------------------------
	# updateExperience
	#
	#
	#--------------------------------------------------------------
	def updateExperience(self, email, experience, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[email, experience, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateExperience",parms, sig)
			print "updateExperience request processed"

	#--------------------------------------------------------------
	# deletePhoto
	#
	#
	#--------------------------------------------------------------
	def deletePhoto(self, email, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String"]
			parms=[email, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"deletePhoto",parms, sig)
			print "deletePhoto request processed"

	#--------------------------------------------------------------
	# updateDescriptionByUserId
	#
	#
	#--------------------------------------------------------------
	def updateDescriptionByUserId(self, userid, description, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[userid, description, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateDescriptionByUserId",parms, sig)
			print "updateDescriptionByUserId request processed"

	#--------------------------------------------------------------
	# updateExperienceByUserId
	#
	#
	#--------------------------------------------------------------
	def updateExperienceByUserId(self, userid, experience, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[userid, experience, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateExperienceByUserId",parms, sig)
			print "updateExperienceByUserId request processed"

	#--------------------------------------------------------------
	# deletePhotoByUserId
	#
	#
	#--------------------------------------------------------------
	def deletePhotoByUserId(self, userid, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String"]
			parms=[userid, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"deletePhotoByUserId",parms, sig)
			print "deletePhotoByUserId request processed"

	#--------------------------------------------------------------
	# updateUser
	#
	#
	#--------------------------------------------------------------
	def updateUser(self, emailParam, **userData):
			userDataMap = pyDictionaryToMap(userData)

			orgIdParm = orgIdGlobal
			orgIdFromCommandLine = userDataMap.get("orgId")

			if (orgIdFromCommandLine):
				orgIdParm = orgIdFromCommandLine
				userDataMap.remove("orgId")

			sig = ["java.lang.String", "java.util.HashMap", "java.lang.String"]
			parms=[emailParam, userDataMap, orgIdParm]

			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateUser",parms, sig)
			if rc != None:
				print rc
			else:
				print "updateUser request processed"

	#--------------------------------------------------------------
	# updateUserByUserId
	#
	#
	#--------------------------------------------------------------
	def updateUserByUserId(self, userIdParam, **userData):
			userDataMap = pyDictionaryToMap(userData)

			orgIdParm = orgIdGlobal
			orgIdFromCommandLine = userDataMap.get("orgId")

			if (orgIdFromCommandLine):
				orgIdParm = orgIdFromCommandLine
				userDataMap.remove("orgId")

			sig = ["java.lang.String", "java.util.HashMap", "java.lang.String"]
			parms=[userIdParam, userDataMap, orgIdParm]

			rc = AdminControl.invoke_jmx(self.getMBeanName(),"updateUserByUserId",parms, sig)
			#print "updateUserByUserId return"
			#print rc
			if rc != None:
				print "updateUserByUserId request failed.  Explanation: "
				print rc
			else:
				print "updateUserByUserId request processed"

	#--------------------------------------------------------------
	# getRoles (by email)
	#
	#
	#--------------------------------------------------------------
	def getRoles(self, email, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.lang.String']
			parms=[email, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"getUserRoles",parms, sig)
			print rc

	#--------------------------------------------------------------
	# getRolesByUserId
	#
	#
	#--------------------------------------------------------------
	def getRolesByUserId(self, userid, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.lang.String']
			parms=[userid, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"getUserRolesByUserId",parms, sig)
			print rc

	#--------------------------------------------------------------
	# setRoles (by email)
	#
	#
	#--------------------------------------------------------------
	def setRoles(self, email, roleset, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.util.ArrayList', 'java.lang.String']
			parms=[email, roleset, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"setUserRoles", parms, sig)
			print rc

	#--------------------------------------------------------------
	# setRolesByUserId
	#
	#
	#--------------------------------------------------------------
	def setRolesByUserId(self, userid, roleset, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.util.ArrayList', 'java.lang.String']
			parms=[userid, roleset, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"setUserRolesByUserId", parms, sig)
			print rc

	#--------------------------------------------------------------
	# set user role (by email)
	#
	#
	#--------------------------------------------------------------
	def setRole(self, email, role, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.lang.String', 'java.lang.String']
			parms=[email, role, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"setUserRole", parms, sig)
			print rc

	#--------------------------------------------------------------
	# set user role (by user ID)
	#
	#
	#--------------------------------------------------------------
	def setRoleByUserId(self, userid, role, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String', 'java.lang.String', 'java.lang.String']
			parms=[userid, role, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"setUserRoleByUserId", parms, sig)
			print rc


	#--------------------------------------------------------------
	# setRole batch users (by email)
	#
	# Arguments
	#     emailAddrFile -- file local to wsadmin client machine that contains a 
	#                      list of emails address. One address on each line.
	#
	#--------------------------------------------------------------
	def setBatchRole(self, role, filename, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal

			emailAddresses = None
			try:
				file = open(filename, "r")
				emailAddresses = file.readlines()
			except:
			       	c, i, tb = sys.exc_info()	    
				print "Exception - ", c, i
				return

			if not emailAddresses:
				print filename + " has no contents"
#			else:
#				print "%s %s %i %s" % (filename, " has", len(emailAddresses), "lines")

			i = 1
			eAddrs = HashSet()
			for s in emailAddresses:
				str = s.strip()
				if (len(str) > 0):
					if (str.find('@') >= 0):
						eAddrs.add(str)
#						print "%s %i %s %s" % ("Adding email address [", i, "] :", str)
						i = i + 1
					else:
						print "Skipping", str, "because it is not a properly formatted email address."

			if eAddrs.size() == 0:
				print filename + " has no valid email addresses"
			else:
				sig = ['java.lang.String', "java.util.HashSet", 'java.lang.String']
				parms=[role, eAddrs, orgId]
				rc = AdminControl.invoke_jmx(self.getMBeanName(),"setBatchUserRole", parms, sig)
				print "setBatchRole request processed"
				print rc

			file.close()
			file = None

	#--------------------------------------------------------------
	# setRoleByBatchUserId batch users (by user ID)
	#
	# Arguments
	#     userIDFile -- file local to wsadmin client machine that contains a 
	#                   list of user IDs. One user ID on each line.
	#
	#--------------------------------------------------------------
	def setBatchRoleByUserId(self, role, filename, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal

			userIDs = None
			try:
				file = open(filename, "r")
				userIDs = file.readlines()
			except:
			       	c, i, tb = sys.exc_info()	    
				print "Exception - ", c, i
				return

			if not userIDs:
				print filename + " has no user IDs"
#			else:
#				print "%s %s %i %s" % (filename, " has", len(userIDs), "lines")

			i = 1
			ids = HashSet()
			for s in userIDs:
				str = s.strip()
				if (len(str) > 0):
					ids.add(str)
#					print "%s %i %s %s" % ("Adding userID [", i, "] :", str)
					i = i + 1

			if ids.size() == 0:
				print filename + " has no valid user IDs"
			else:
				sig = ['java.lang.String', "java.util.HashSet", 'java.lang.String']
				parms=[role, ids, orgId]
				rc = AdminControl.invoke_jmx(self.getMBeanName(),"setBatchUserRoleByUserId", parms, sig)
				print "setBatchRole request processed"
				print rc

			file.close()
			file = None

	#--------------------------------------------------------------
	# getTenantProfileType
	#
	# Arguments
	#
	#--------------------------------------------------------------
	def getTenantProfileType(self, orgId="_default_"):
#			print "retrieve profile-type for " + orgId
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String']
			parms=[orgId]
			print "Getting PT for org : " + orgId
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"getTenantProfileType", parms, sig)
			print "getTenantProfileType request processed"
			print rc
	
	#--------------------------------------------------------------
	# deleteTenantProfileType
	#
	# Arguments
	#
	#--------------------------------------------------------------
	def deleteTenantProfileType(self, orgId="_default_"):
#			print "delete profile-type for " + orgId
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String']
			parms=[orgId]
			print "Resetting profile-type for org : " + orgId + " to default"
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"deleteTenantProfileType", parms, sig)
			print "deleteTenantProfileType request processed"
			print rc
			
	#--------------------------------------------------------------
	# setTenantProfileType
	#
	# Arguments
	#     filename -- file local to wsadmin client machine that contains 
	#                 the update, XML string,  for a tenant's ProfileType 
	#
	#--------------------------------------------------------------
	def setTenantProfileType(self, filename, orgId="_default_"):
#			print "set profile-type for " + orgId + " using " + filename
			if (orgId == "_default_"):
				orgId = orgIdGlobal

			xmlStrings = None
			try:
				file = open(filename, "r")
#				print filename + " is open"
				xmlStrings = file.readlines()
#				print "read file"
			except:
			       	c, i, tb = sys.exc_info()	    
				print "Exception - ", c, i
				return

			if not xmlStrings:
				print filename + " has no contents"
#			else:
#				print "%s %s %i %s" % (filename, " has", len(xmlStrings), "lines")

			i = 1
#			print "Process file lines"
			strProfileType = ""
			for s in xmlStrings:
				str = s.strip()
#				print "line " + str
				if (len(str) > 0):
#					print "append " + str
					strProfileType = strProfileType + (str)
#					print "%s %i %s %s" % ("Adding xml string [", i, "] :", str)
					i = i + 1
#				else:
#					print "%s %i %s" % ("Length of string [", i, "] is <= 0")

#			print strProfileType
			if strProfileType == "":
				print filename + " has no valid xml content"
			else:
				sig = ['java.lang.String', 'java.lang.String']
				parms=[orgId, strProfileType]
				print "Setting PT for org : " + orgId + " " + strProfileType
				rc = AdminControl.invoke_jmx(self.getMBeanName(),"setTenantProfileType", parms, sig)
				print "setTenantProfileType request processed"
				print rc

			file.close()
			file = None
	
	#--------------------------------------------------------------
	# getTenantPolicy
	#
	# Arguments
	#     merged - boolean indicating if the org's policy is to be
	#              merged with the default/base policy
	#
	#--------------------------------------------------------------
	def getTenantPolicy(self, orgId="_default_", merged="false"):
#			print filename + " " + " " + merged + " " +orgId
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String','java.lang.String']
			parms=[orgId, merged]
			print "Getting Policy definition for org : " + orgId
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"getTenantPolicy", parms, sig)
			print "getTenantPolicy processed"
			print rc
	#--------------------------------------------------------------
	# deleteTenantPolicy
	#
	# Arguments
	#
	#--------------------------------------------------------------
	def deleteTenantPolicy(self, orgId="_default_"):
#			print "delete profile-policy for " + orgId
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ['java.lang.String']
			parms=[orgId]
			print "Resetting policy for org : " + orgId + " to default"
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"deleteTenantPolicy", parms, sig)
			print "deleteTenantPolicy request processed"
			#print rc
			printIfNotNull(rc)
	#--------------------------------------------------------------
	# setTenantPolicy
	#
	# Arguments
	#     filename -- file local to wsadmin client machine that contains 
	#                 the update, XML string,  for a tenant's Policy 
	#
	#--------------------------------------------------------------
	def setTenantPolicy(self, filename, orgId="_default_", commit="true"):
#			print "set profile policy for " + orgId + " using " + filename
			if (orgId == "_default_"):
				orgId = orgIdGlobal

			xmlStrings = None
			try:
				file = open(filename, "r")
#				print filename + " is open"
				xmlStrings = file.readlines()
#				print "read file"
			except:
			       	c, i, tb = sys.exc_info()	    
				print "Exception - ", c, i
				return

			if not xmlStrings:
				print filename + " has no contents"
#			else:
#				print "%s %s %i %s" % (filename, " has", len(xmlStrings), "lines")

			i = 1
#			print "Process file lines"
			strPolicy = ""
			for s in xmlStrings:
				str = s.strip()
#				print "line " + str
				if (len(str) > 0):
#					print "append " + str
					strPolicy = strPolicy + (str)
#					print "%s %i %s %s" % ("Adding xml string [", i, "] :", str)
					i = i + 1
#				else:
#					print "%s %i %s" % ("Length of string [", i, "] is <= 0")

#			print strPolicy
			if strPolicy == "":
				print filename + " has no valid xml content"
			else:
				sig = ['java.lang.String', 'java.lang.String', 'java.lang.String']
				parms=[orgId, strPolicy, commit]
				print "Setting Policy for org : " + orgId + " policy: " + strPolicy
				rc = AdminControl.invoke_jmx(self.getMBeanName(),"setTenantPolicy", parms, sig)
				print "setTenantPolicy request processed"
				#print rc
				printIfNotNull(rc)

			file.close()
			file = None

	#--------------------------------------------------------------
	# publishUserData
	#
	#
	#--------------------------------------------------------------
	def publishUserData(self, email, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String"]
			parms=[email, orgId]
			print "Publishing UserData for org : " + orgId + " email: " + email
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"publishUserData",parms, sig)
			if rc != None:
				print rc
			else:
				print "publishUserData request processed"

			
	#--------------------------------------------------------------
	# publishUserDataByUserId
	#
	#
	#--------------------------------------------------------------
	def publishUserDataByUserId(self, userId, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String"]
			parms=[userId, orgId]
			print "Publishing UserData for org : " + orgId + " userId: " + userId
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"publishUserDataByUserId",parms, sig)
			if rc != None:
				print rc
			else:
				print "publishUserDataByUserId request processed"


	#--------------------------------------------------------------
	# activateUserByUserId
	#
	#
	#--------------------------------------------------------------
	def activateUserByUserId(self, userId, **userData):
			userDataMap = pyDictionaryToMap(userData)
			#print "userDataMap1"
			#print userDataMap
			#print "orgIdGlobal - activate"
			#print orgIdGlobal
			orgIdParm = orgIdGlobal
			#print "orgIdParm"
			#print orgIdParm
			orgIdFromCommandLine = userDataMap.get("orgId")
			#print "orgIdFromCommandLine"
			#print orgIdFromCommandLine
			if (orgIdFromCommandLine):
				#print "found orgId"
				orgIdParm = orgIdFromCommandLine
				userDataMap.remove("orgId")
			sig = ["java.lang.String", "java.util.HashMap", "java.lang.String"]
			parms=[userId, userDataMap, orgIdParm]

			#print "userDataMap2"
			#print userDataMap
			#print "orgIdParm"
			#print orgIdParm
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"activateUserByUserId",parms, sig)
			if rc != None:
				print rc
			else:
				print "activateUserByUserId request processed"

	#--------------------------------------------------------------
	# inactivateUser
	#
	#
	#--------------------------------------------------------------
	def inactivateUser(self, email, emailTransfer=None, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[email, emailTransfer, orgId]
			#print "orgId"
			#print orgId
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"inactivateUser",parms, sig)
			if rc != None:
				print rc
			else:
				print "inactivateUser request processed"

	#--------------------------------------------------------------
	# inactivateUserByUserId
	#
	#
	#--------------------------------------------------------------
	def inactivateUserByUserId(self, userId, userIdTransfer=None, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[userId, userIdTransfer, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"inactivateUserByUserId",parms, sig)
			if rc != None:
				print rc
			else:
				print "inactivateUserByUserId request processed"

	#--------------------------------------------------------------
	# swapUserAccessByUserId
	#
	#
	#--------------------------------------------------------------
	def swapUserAccessByUserId(self, userToActivate, userToInactivate, orgId="_default_"):
			if (orgId == "_default_"):
				orgId = orgIdGlobal
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[userToActivate, userToInactivate, orgId]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"swapUserAccessByUserId",parms, sig)
			if rc != None:
				print rc
			else:
				print "swapUserAccessByUserId request processed"

	#--------------------------------------------------------------
	# purgeEventLogs
	#
	#
	#--------------------------------------------------------------
	def purgeEventLogs(self):
			rc = AdminControl.invoke(str(self.getMBeanName()),"purgeEventLogs")
			print "purgeEventLogs request processed"

	#--------------------------------------------------------------
	# purgeEventLogsByDates
	#
	#
	#--------------------------------------------------------------
	def purgeEventLogsByDates(self, startDate, endDate):
			sig = ["java.lang.String", "java.lang.String"]
			parms=[startDate, endDate]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"purgeEventLogs",parms, sig)
			print "purgeEventLogsByDates request processed"

	#--------------------------------------------------------------
	# purgeEventLogsByDatesAndEventName
	#
	#
	#--------------------------------------------------------------
	def purgeEventLogsByEventNameAndDates(self, eventName, startDate, endDate):

			print "purgeEventLogsByDatesAndEventName processing..."
			sig = ["java.lang.String", "java.lang.String", "java.lang.String"]
			parms=[eventName, startDate, endDate]
			rc = AdminControl.invoke_jmx(self.getMBeanName(),"purgeEventLogs",parms, sig)
			print "purgeEventLogsByDatesAndEventName request processed"

	#--------------------------------------------------------------
	# getMBeanName
	#
	#--------------------------------------------------------------

	def getMBeanName(self):
		global initializeOk
		if (not self.gMBeanName):
				profBeansList = self.getMBeans()
				if(len(profBeansList) > 1):
					# > 1 bean instance, have admin select
					self.setMBeanName(self.promptAdminForBean(profBeansList))
				elif (len(profBeansList) < 1):
					print "No Profiles services found"
					initializeOk = 0
					return
				else:
					#only one bean instance, use it.
					self.setMBeanName(profBeansList[0])


		return self.gMBeanName

	def getBeanNameStr(self):
		return self.beanNameStr

	def getBeanNameStrBeg(self):
		if (not self.gMBeanName):
			self.getMBeanName()

		return self.beanNameStrBeg

	def getBeanNameStrEnd(self):
		return self.beanNameStrEnd


	#--------------------------------------------------------------
	# setMBeanName
	#
	# Arguments
	#     beanName  e.g. WebSphere:cell=myCell,name=ProfilesAdmin,type=LotusConnections,node=myNode,process=myProcess
	#--------------------------------------------------------------
	def setMBeanName(self, beanName):
		import javax.management as mgmt
		try:
			beanObjName = mgmt.ObjectName(beanName)
		except:
		       	c, i, tb = sys.exc_info()
			print "Exception - ", beanName + " is not a valid MBean name."
		else:
			#Determine if instance is registered
			profBean =AdminControl.queryNames(str(beanObjName))
			if(len(profBean) == 0):
				print "Error -- system cannot locate mbean with name " + beanName
			else:
#				print "Setting bean name to " + beanName
				self.gMBeanName = beanObjName

				self.beanNameStr = beanName

				beanNameStr = String( self.getBeanNameStr())

				nameIndex = beanNameStr.indexOf("name=")
				#print" nameIndex"
				#print nameIndex
				self.beanNameStrBeg = beanNameStr.substring(0, nameIndex + 5)
				#print "self.beanNameStrBeg"
				#print self.beanNameStrBeg
				typeIndex = beanNameStr.indexOf("type=")
				#print" typeIndex"
				#print typeIndex
				self.beanNameStrEnd = beanNameStr.substring( typeIndex - 1)
				#print "self.beanNameStrEnd"
				#print self.beanNameStrEnd

				print "Connecting to " + reorderString(beanNameStr)

	#--------------------------------------------------------------
	# listMBeans
	#	Writes list of mbeans that this service has registered
	# Arguments
	#
	#--------------------------------------------------------------
	def listMBeans(self, *args):
		if(args and len(args) > 0):
			profBeansList = args[0]
		else:
			profBeansList = self.getMBeans()
		i=1
		for s in profBeansList:
		        print "%i: %s" % (i, s)
		        i=i+1

	def promptAdminForBean(self, profBeansList):
		while (1):
			if (bAskForNodeProfiles):
				self.listMBeans()
				print "Which service do you want to connect to?"
				response = sys.stdin.readline()
				print ""
			else:
				response = "1"
			try:
				j = int(response) - 1
				return profBeansList[j]
			except:
				print "Invalid selection, specify an index number between 1 and %i." % len(profBeansList)

	def getMBeans(self):
		if (bAskForNodeProfiles or (serviceNodeNameProfiles == None)):
			profBeans =AdminControl.queryNames("name=ProfilesAdmin,type=LotusConnections,*")
		else:
			profBeans =AdminControl.queryNames("name=ProfilesAdmin,type=LotusConnections,node=" + serviceNodeNameProfiles + ",*")
		retval = None
		if(profBeans and (len(profBeans) > 0)):
			profBeansList = profBeans.splitlines()
			retval = profBeansList
		else:
			retval = []

		return retval

	def buildMBeanName(self, cell, node, process):
		self.gCellName = cell
		self.gNodeName = node
		self.gProcessName = process
		self.objNameString = AdminControl.completeObjectName("WebSphere:cell=" + cell +",name=ProfilesAdmin,type=LotusConnections,node=" + node + ",process=" + process)
		import javax.management as mgmt
		self.gMBeanName = mgmt.ObjectName(self.objNameString)
		print "MBean Name: "
		print self.gMBeanName
		print type(self.gMBeanName)
		if(str(self.gMBeanName) == "*:*"):
			print "Error -- system cannot locate mbean with name " + self.beanNameStr

#-----------------------------------------------------------------
# ProfConfigFileValidator  Class
#-----------------------------------------------------------------
#
#  The purpose of this script is to provide validation for an xml file against
#  collocated schema file(s)
#
#
#
#
#-----------------------------------------------------------------
import sys, java
import os
from java.lang import System
import exceptions
from com.ibm.ws.scripting import ScriptingException


from java.io import File;
from java.io import FileInputStream;
from java.io import FileNotFoundException;
from java.io import IOException;
from java.io import InputStream;

from javax.xml.parsers import ParserConfigurationException;
from javax.xml.parsers import SAXParser;
from javax.xml.parsers import SAXParserFactory;

from org.xml.sax import Attributes;
from org.xml.sax import EntityResolver;
from org.xml.sax import InputSource;
from org.xml.sax import SAXException;
from org.xml.sax import SAXParseException;
from org.xml.sax import XMLReader;
from org.xml.sax.helpers import DefaultHandler;


class ProfConfigFileValidator:

	def __init__(self, filename):
		self.fileName = filename
		self.valid = 0
		self.debugEnabled = 0
		self.xmlReader = None
		self.sp = None

	def logDebug(self, msg):
		print msg

	def log(self, msg):
		print msg

	def logError(self, msg, e):
		print("Error: " + msg);
		print("Exception: " + e.getMessage());


	def validateDoc(self):
		if (self.debugEnabled):
			self.logDebug("validateDoc entry");
	        try:
	            spf = SAXParserFactory.newInstance();
	            spf.setNamespaceAware(1);
	            spf.setValidating(1);
	            spf.setFeature("http://xml.org/sax/features/validation", 1);
	            spf.setFeature("http://apache.org/xml/features/validation/schema", 1);
	            spf.setFeature("http://apache.org/xml/features/validation/schema-full-checking", 1);

	            self.sp = spf.newSAXParser();
	            self.sp.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

	            self.xmlReader = self.sp.getXMLReader();

	            defaultHandler = ProfDefaultContentHandler(self.debugEnabled);

	            self.xmlReader.setEntityResolver(ProfSchemaLoader());

	            self.xmlReader.setContentHandler(defaultHandler);

	            self.xmlReader.setErrorHandler(defaultHandler);

	            self.xmlReader.parse(self.fileName);

	            self.log(self.fileName + " is valid");

	        except SAXException:
	        	raise

	        except ParserConfigurationException:
			raise

	        if (self.debugEnabled):
			self.logDebug("validateDoc exit");


class ProfDefaultContentHandler (DefaultHandler):

	def __init__(self, debugEnabled):
		self.debugEnabled=debugEnabled

	def warning(self, SAXParserException_e):
		print SAXParserException_e


        def error(self, SAXParseException_e):
        	print "Exception - " , SAXParseException_e
	     	raise SAXParseException_e;


        def fatalError(self, SAXParseException_e):
         	print "Exception - " , SAXParseException_e
           	raise SAXParseException_e;


        def startElement(self, s1, s2, s3, a):
        	if (self.debugEnabled):
        		print s1, s2, s3, a


class ProfSchemaLoader(EntityResolver):
	def __init__(self):
		self.FILE_SCHEME = "file://";

	def resolveEntity(self, publicId, systemId):

		schemaFileName = systemId[self.FILE_SCHEME.__len__() :  ]

		print "Loading schema file for validation:" , schemaFileName
		stream = FileInputStream(schemaFileName)

		retVal = InputSource(stream)

		return retVal




#-----------------------------------------------------------------
# ProfConfigFileReaderUpdater  Class
#-----------------------------------------------------------------
#
#  The purpose of this script is to provide a class for reading updating the node attributes
#  and values of an xml.
#
#
#
#
#-----------------------------------------------------------------

from java.io import FileInputStream;
from java.io import FileNotFoundException;
from java.io import FileWriter;
from java.io import IOException;
from java.io import InputStream;

from javax.xml.parsers import DocumentBuilder;
from javax.xml.parsers import DocumentBuilderFactory;
from javax.xml.parsers import ParserConfigurationException;
from javax.xml.transform import OutputKeys;
from javax.xml.transform import Transformer;
from javax.xml.transform import TransformerException;
from javax.xml.transform import TransformerFactory;
from javax.xml.transform import TransformerFactoryConfigurationError;
from javax.xml.transform.dom import DOMSource;
from javax.xml.transform.stream import StreamResult;

from org.apache.xpath import CachedXPathAPI;
from org.w3c.dom import Document;
from org.w3c.dom import Element;
from org.w3c.dom import Node;
from org.w3c.dom import NodeList;
from org.xml.sax import SAXException;


class ProfConfigFileReaderUpdater:

	def __init__(self, filename):
		self.fileName = filename;
		self.xpath = CachedXPathAPI()
		self.debugEnabled = 0
		self.dbfactory = DocumentBuilderFactory.newInstance();
		self.dbfactory.setNamespaceAware(1);
		self.dbfactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
							   "http://www.w3.org/2001/XMLSchema");
		self.dbfactory.setIgnoringElementContentWhitespace(1);

		try:
			documentBuilder = self.dbfactory.newDocumentBuilder();

			configIS = FileInputStream(self.fileName);

			self.doc = documentBuilder.parse(configIS);

		except ParserConfigurationException, e:
			print e
			raise e

		except  FileNotFoundException, e:
			print e
			raise e

		except SAXException, e:
			print e
			raise e

		except IOException, e:
			print e
			raise e

	def saveDoc(self):
		transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		fw =FileWriter(self.fileName);
		result = StreamResult(fw);
		source = DOMSource(self.doc);
		transformer.transform(source, result);
		fw.close();

	def getNodeProperty(self, xpathStr, attribute):
		config = self.doc.getDocumentElement();
		nl = self.xpath.selectNodeList(config, xpathStr);
		if(nl.getLength() > 0):
			node = nl.item(0);
			attNode = node.getAttributes().getNamedItem(attribute);
			return attNode.getNodeValue()
		else:
			return "Unknown"


	def updateNodeProperty(self, xpathStr, attribute, value):
		config = self.doc.getDocumentElement();
		nl = self.xpath.selectNodeList(config, xpathStr);
		if(nl.getLength() > 0):
			node = nl.item(0);
			attNode = node.getAttributes().getNamedItem(attribute);
			msg = "Changing " + attribute + " from " + attNode.getNodeValue() + " to " + value
			self.log(msg)
			attNode.setNodeValue(value);

		return 1;

	def getNodeValue(self, xpathStr):
		config = self.doc.getDocumentElement();
		nl = self.xpath.selectNodeList(config, xpathStr);
		if(nl.getLength() > 0):
			node = nl.item(0);
			node = node.getFirstChild();
			return node.getNodeValue();
		else:
			return "Unknown"


	def updateNodeValue(self, xpathStr, value):
		config = self.doc.getDocumentElement();
		nl = self.xpath.selectNodeList(config, xpathStr);
		if(nl.getLength() > 0):
			node = nl.item(0);
			parent = node.getParentNode();
			nodeName = None;
			if (parent):
				nodeName = parent.getLocalName();

			if(not nodeName):
				nodeName = node.getLocalName()
			else:
				nodeName = nodeName + "." + node.getLocalName()

			node = node.getFirstChild();
			nodeVal = node.getNodeValue();
			msg = "Changing " + nodeName + " from " + nodeVal + " to " + value
			self.log(msg)
			node.setNodeValue(value);

		return 1


	def logDebug(self, msg):
		print msg

	def log(self, msg):
		print msg

	def logError(self, msg, exception):
		print "Error: ", msg
		print "Exception: ", exception

#-----------------------------------------------------------------
# ProfilesMetricsServiceMBean  Class
#-----------------------------------------------------------------
#
#
#
#
#
#-----------------------------------------------------------------
class ProfilesMetricsServiceMBean:
  def fetchMetrics(self):
    global AdminControl
    x = ProfilesService.getBeanNameStrBeg()
    if ( not x):
        return
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesMetricsService" + ProfilesService.getBeanNameStrEnd())
    #print "\n start fetchMetrics 1: " + objNameString
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = []
    signature = []
    mets = AdminControl.invoke_jmx(objName, 'fetchMetrics', parms, signature)
    return mets

  def fetchMetric(self, metricName):
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesMetricsService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [metricName]
    signature = ['java.lang.String']
    mets = AdminControl.invoke_jmx(objName, 'fetchMetric', parms, signature)
    return mets

  def saveMetricToFile(self, absFileName, sampleCount, fieldKey):
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesMetricsService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [absFileName, sampleCount, fieldKey]
    signature = ['java.lang.String', 'java.lang.Integer', 'java.lang.String']
    strmets = AdminControl.invoke_jmx(objName, 'saveMetricToFile', parms, signature)
    return strmets

  def saveMetricsToFile(self, absFileName, sampleCount, fieldKeys):
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesMetricsService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [absFileName, sampleCount, fieldKeys]
    signature = ['java.lang.String', 'java.lang.Integer', 'java.util.ArrayList']
    strmets = AdminControl.invoke_jmx(objName, 'saveMetricsToFile', parms, signature)
    return strmets

  def fetchMetricsInternalTest(self):
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesMetricsService" + ProfilesService.getBeanNameStrEnd())
    print "ProfilesMetricsService objNameString: " + objNameString
    print " "
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = []
    signature = []
    mets = AdminControl.invoke_jmx(objName, 'fetchMetrics', parms, signature)
    return mets


class ProfilesInternalTestClass:
  def InternalTestAll (self):
    print " "
    #
    metrics=ProfilesMetricsService.fetchMetricsInternalTest()
    print "metrics: "
    print metrics
    print " "
    #

#-----------------------------------------------------------------
# ProfilesScheduledTaskServiceMBean  Class
#-----------------------------------------------------------------
#
#
#
#
#
#-----------------------------------------------------------------
class ProfilesScheduledTaskServiceMBean:
  def pauseSchedulingTask(self, taskName):
#    if not TypeValidator.validateString(taskName, "taskName"):
#      return 0
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesScheduledTaskService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [taskName]
    signature = ['java.lang.String']
    AdminControl.invoke_jmx(objName, 'pauseSchedulingTask', parms, signature)
    print taskName + " paused"
    return

  def resumeSchedulingTask(self, taskName):
#    if not TypeValidator.validateString(taskName, "taskName"):
#      return 0
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesScheduledTaskService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [taskName]
    signature = ['java.lang.String']
    AdminControl.invoke_jmx(objName, 'resumeSchedulingTask', parms, signature)
    print taskName + " resumed"
    return

  def forceTaskExecution(self, taskName, executeSynchronously = "false"):
#    if not TypeValidator.validateString(taskName, "taskName"):
#      return 0
#    if not TypeValidator.validateBoolean(executeSynchronously, "executeSynchronously"):
#      return 0
    executeSynchronously = str(executeSynchronously).strip().lower()
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesScheduledTaskService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [taskName, executeSynchronously]
    signature = ['java.lang.String', 'java.lang.String']
    mode = None
    rc = 1
    try:
      rc = AdminControl.invoke_jmx(objName, 'forceTaskExecution', parms, signature)
      if executeSynchronously and "true" == executeSynchronously:
        mode = "synchronously and has completed. Please check the server logs for messages."
      else:
        mode = "asynchronously and will run until complete. Please check the server logs for messages."
      print "Task", taskName, "was executed", mode
    except:
      rc = 0
      if executeSynchronously and "true" == executeSynchronously:
        mode = "synchronously due to an exception. Please check the server logs for messages."
      else:
        mode = "asynchronously due to an exception. Please check the server logs for messages."
      print "Task", taskName, "cannot be executed", mode
    return rc

  def getTaskDetails(self, taskName):
#    if not TypeValidator.validateString(taskName, "taskName"):
#      return
    global AdminControl
    objNameString = AdminControl.completeObjectName( ProfilesService.getBeanNameStrBeg() + "ProfilesScheduledTaskService" + ProfilesService.getBeanNameStrEnd())
    import  javax.management  as  mgmt
    objName =  mgmt.ObjectName(objNameString)
    parms = [taskName]
    signature = ['java.lang.String']
    taskDetailHashMap =[]
    try:
       taskDetailHashMap = AdminControl.invoke_jmx(objName, 'getTaskDetails', parms, signature)
       print "The task details with the name", taskName, " are listed below."
       return taskDetailHashMap
    except:
       print "The details of task with the name", taskName, " cannot be listed due to an exception. Please check the server logs for messages."
       return


global ProfilesConfigService
global ProfilesMetricsService
global ProfilesScheduledTaskService

ProfilesConfigService = ProfilesConfigurationService()
ProfilesService =  ProfilesServiceMBean()
ProfilesService.getMBeanName()

ProfilesMetricsService =  ProfilesMetricsServiceMBean()
pits = ProfilesInternalTestClass()
ProfilesScheduledTaskService = ProfilesScheduledTaskServiceMBean()

EMPLOYEE = "employee"
EMPLOYEE_EXTENDED = "employee.extended"
VISITOR = "visitor"
DEFAULT_ROLE = EMPLOYEE

#ProfilesMetricsService.fetchMetrics()

if (initializeOk == 1):
    print "Profiles Administration initialized"

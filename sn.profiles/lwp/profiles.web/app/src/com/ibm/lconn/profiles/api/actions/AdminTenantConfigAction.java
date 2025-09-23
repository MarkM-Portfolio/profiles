/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.InvalidAttributeValueException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.abdera.parser.ParseException;
import org.apache.abdera.writer.StreamWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.connections.highway.common.api.HighwayUserSessionInfo;

import com.ibm.lconn.core.util.ResourceBundleHelper;

import com.ibm.lconn.profiles.api.actions.APIException.ECause;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.types.ProfileType;
import com.ibm.lconn.profiles.config.types.ProfileTypeConfig;
import com.ibm.lconn.profiles.config.types.ProfileTypeConstants;
import com.ibm.lconn.profiles.config.types.ProfileTypeHelper;
import com.ibm.lconn.profiles.config.types.ProfileTypeImpl;
import com.ibm.lconn.profiles.config.types.ProfilesTypesCache;
import com.ibm.lconn.profiles.config.types.Property;
import com.ibm.lconn.profiles.config.types.PropertyImpl;

import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.constants.ProfilesServiceConstants;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.TDIProfileService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.lconn.profiles.internal.util.ProfilesHighway;

import com.ibm.peoplepages.util.appcntx.AppContextAccess;

public class AdminTenantConfigAction extends APIAction implements AtomConstants 
{
	private final static String CLASS_NAME = AdminTenantConfigAction.class.getSimpleName();

	private final static Log LOG = LogFactory.getLog(AdminTenantConfigAction.class);
	private ResourceBundleHelper helper;

	private final TDIProfileService _tdiProfileSvc = AppServiceContextAccess.getContextObject(TDIProfileService.class);

	// tenantConfig.do is only exposed in MT / SC environment
	private static boolean _multiTenantConfigEnabled;
	static {
		_multiTenantConfigEnabled = LCConfig.instance().isMTEnvironment();
	}
	
	private boolean _isTesting = false;
	private boolean _isMTOverride = false;

	private static final class Bean {
		boolean isAuthRequest = false;

		String  orgId  = null;

		String  payload = null;

//		boolean isXML  = false;
//		boolean isJSON = false;

		public Bean() {}
	}

	private Bean getBeanForPOST(HttpServletRequest request) throws Exception {
		return getBean(request, true);
	}
	private Bean getBeanForGET(HttpServletRequest request) throws Exception {
		return getBean(request, false);
	}
	private Bean getBeanForDELETE(HttpServletRequest request) throws Exception {
		return getBean(request, false);
	}
	
	private Bean getBean(HttpServletRequest request, boolean isPOST) throws Exception {
		Bean bean = getActionBean(request, Bean.class);

		if (bean == null) {
			bean = new Bean();

			bean.isAuthRequest = AppContextAccess.isAuthenticated();

			//if (bean.isAuthRequest)
			//{
			//}
			// else
			// ... not logged in case with no param fails on ROLE_ADMIN test below ...

			// verify that the user is in the 'admin' role for Profiles
//TODO - are we allowing Org-admin to do this ? or just BSS ??
			boolean isAdmin = AppContextAccess.isUserInRole(ProfilesServiceConstants.ROLE_ADMIN);
					isAdmin = AppContextAccess.isUserAnAdmin();
//TODO - are we allowing Org-admin to do this ? or just BSS ??
			if (!isAdmin){
				throw new APIException(ECause.FORBIDDEN);
			}

			bean.orgId = getRequestParamStr(request, ProfileTypeConstants.TYPES_ORG_ID, null);

			if (isPOST){
				bean.payload = AdminActionHelper.getProfileTypeAsString(request.getInputStream());
			}

			storeActionBean(request, bean);
		}
		return bean;
	}

	@Override
	protected long getLastModified(HttpServletRequest request) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		checkMTTestOverride(request); // testing MT on ST env

		// tenantConfig.do API is only allowed in SC / MT environment
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForPOST(request);
			doPost(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}

	private void doPost(Bean bean, HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException, InvalidAttributeValueException, APIException, Exception
	{
		String methodName = "doPost";

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}
		// first pass to check request data
		String tenantId = checkRequestData(request, response, bean);

		// pay-load is supplied on a POST; verify that is present & has a value; validation of content will happen during the parse
		if (StringUtils.isEmpty(bean.payload)) {
			LOG.warn(CLASS_NAME + "." + methodName + " invalid orgId on ProfileType update request. This org id is not recognized by this data set: " + bean.orgId);
			throw new APIException(ECause.INVALID_REQUEST);
		}
		else {
			LOG.info(CLASS_NAME + "." + methodName + " received :\n" + bean.payload); 
		}

		ProfileType inputProfileType = null;
		String inputExtensionString = bean.payload;
		// Profiles only supports XML pay-load
		if (inputExtensionString.startsWith("<")) {
			inputProfileType = ProfileTypeHelper.getProfileTypeFromXMLString(inputExtensionString, tenantId, true); //true = xml needs validation
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : inputProfileType="+inputProfileType);
		}
		if (null == inputProfileType) {
			LOG.warn(CLASS_NAME + "." + methodName + " invalid ProfileType payload detected");
			returnErrorMsgToClient(request, response, "error.atomInvalidAPIPayload", null);
			throw new APIException(ECause.INVALID_XML_CONTENT);
		}
		
		// this constant should be put into sn.infra\highway\...\HighwaySettings.java so that it can be commonly referenced
		String settingName = ProfileTypeConstants.TYPES_DEFINITION; // profiles.org.type.definition
		
		// put the extended attributes definition into Highway
		HighwayUserSessionInfo highwayUserSessionInfo = ProfilesHighway.instance().getHighwayAdminUserSessionInfo(bean.orgId);
		
		// get existing profile type for org
		String lookupId = ProfileTypeConfig.BASE_TYPE_ID; // "snx:person"
		if (_multiTenantConfigEnabled || _isMTOverride) {
				lookupId = ProfileTypeConfig.MT_BASE_TYPE_ID; // "snx:mtperson"
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : highwayUserSessionInfo="+highwayUserSessionInfo);
		}
		String pt_before = ProfilesHighway.instance().getProfileExtensionSetting(settingName, highwayUserSessionInfo);
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : pt_before="+pt_before);
		}
		
		if (pt_before != null && pt_before.length() > 0 && pt_before.trim().compareToIgnoreCase("null") != 0 )
		{
			//process only item properties from Post and update profile type xml that is already saved
			//so that we do not overwrite changes that have been made with wsadmin commands
			inputExtensionString = processItemProperties(pt_before, inputExtensionString);
		}
				

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : update profile type with inputExtensionString="+inputExtensionString);
		}
		boolean success = ProfilesHighway.instance().putProfileExtensionSetting(settingName, inputExtensionString, highwayUserSessionInfo, bean.orgId);

		if (_isTesting) {
			if (LOG.isTraceEnabled()) {
				if (success)
				{
					String profileTypeString = ProfilesHighway.instance().getProfileExtensionSetting(settingName, highwayUserSessionInfo);
					if (inputExtensionString.equals(profileTypeString))
					{
						if (LOG.isTraceEnabled()) {
							LOG.trace(CLASS_NAME + "." + methodName + " : Highway returned expected value\n" + profileTypeString + " for setting : " + settingName);
						}
					}
					else {
						if (null == profileTypeString)
						{
							if (LOG.isTraceEnabled())
								LOG.trace(CLASS_NAME + "." + methodName + " : profileTypeXML is NULL");
						}
						String msg = "Internal error. Highway did NOT return expected value\n" + profileTypeString + " for setting : " + settingName;
						LOG.error(msg);
						throw new javax.naming.directory.InvalidAttributeValueException(msg);
					}
					if (LOG.isTraceEnabled())
						LOG.trace(CLASS_NAME + "." + methodName + (inputExtensionString.equals(profileTypeString) ? "success" : "failed") + " : exiting");
				}
			}
		}

		// Save the ProfileType object into the ProfileTypes cache
		ProfilesTypesCache.getInstance().putProfileType(bean.orgId, inputProfileType);

		if (_isTesting) {
			// get the profile type object from the cache and verify that it is the same as what we saved
			ProfileType testPT = ProfilesTypesCache.getInstance().getProfileType(bean.orgId);
			boolean  isTheSame = validateProfileTypes(inputProfileType, testPT);
			if (! isTheSame) {
				if (LOG.isTraceEnabled()) {
					LOG.error(CLASS_NAME + "." + methodName + " : ProfilesTypesCache - PUT / GET have different values");
				}
				throw new APIException(ECause.INVALID_REQUEST);
			}
		}
	}
	
	protected String processItemProperties(String p_ptBeforeUpdate, String p_inputExtensionString)
	{
		String methodName = "processItemProperties";
		String retVal = p_ptBeforeUpdate;
		
		//parse profile type xml pt_before & after update
		Document docBeforeUpdate, docAfterUpdate;
		Document rootDoc;

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(p_ptBeforeUpdate));
			docBeforeUpdate = builder.parse(is);
			rootDoc = docBeforeUpdate;

			InputSource isUpdated = new InputSource(new StringReader(p_inputExtensionString));
			docAfterUpdate = builder.parse(isUpdated);


			NodeList beforeProperties = docBeforeUpdate.getElementsByTagName("property");
			NodeList afterProperties = docAfterUpdate.getElementsByTagName("property");
			//get item properties from both before update and update from post profile type
			List<String> beforePropertiesNames = getItemProperties(beforeProperties);
			List<String> afterPropertiesNames = getItemProperties(afterProperties);
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "." + methodName + " beforePropertiesNames : " + beforePropertiesNames.toString() + " afterPropertiesNames : "+afterPropertiesNames.toString());
			}

			List<String> propertiesInBeforeAndAfterUpdate = new ArrayList<String>(afterPropertiesNames);
			propertiesInBeforeAndAfterUpdate.retainAll(beforePropertiesNames);
			
			List<String> propertiesOnlyInBeforeUpdate = new ArrayList<String>(beforePropertiesNames);
			propertiesOnlyInBeforeUpdate.removeAll(afterPropertiesNames);

			List<String> propertiesOnlyInAfterUpdate = new ArrayList<String>(afterPropertiesNames);
			propertiesOnlyInAfterUpdate.removeAll(beforePropertiesNames);
			
			
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "." + methodName + " propertiesOnlyInBeforeUpdate : " + propertiesOnlyInBeforeUpdate.toString() + " propertiesOnlyInAfterUpdate : "+propertiesOnlyInAfterUpdate.toString() +" propertiesInBeforeAndAfterUpdate :"+propertiesInBeforeAndAfterUpdate.toString());
			}

			if (propertiesInBeforeAndAfterUpdate.size() > 0)
			{
				//update item prop names
				updateItemProperties(beforeProperties, afterProperties, propertiesInBeforeAndAfterUpdate);
			}
			
			if (propertiesOnlyInBeforeUpdate.size() > 0)
			{
				//remove item attributes not in update
				removeItemProperties(beforeProperties, propertiesOnlyInBeforeUpdate);
			}

			if (propertiesOnlyInAfterUpdate.size() > 0)
			{
				//add item attributes in update
				addItemProperties(rootDoc, afterProperties, propertiesOnlyInAfterUpdate);
			}

			//Now get updated xml for profile type
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(rootDoc);
			StreamResult result = new StreamResult(new StringWriter());
			transformer.transform(source, result);
			retVal = result.getWriter().toString();
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "." + methodName + " updated xml : \n" + retVal);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	protected static Node getChildrenByNodeName(Node node, String nodeName) {
        for (Node childNode = node.getFirstChild(); childNode != null;) {
            Node nextChild = childNode.getNextSibling();
            if (childNode.getNodeName().equalsIgnoreCase(nodeName)) {
                return childNode;
            }
            childNode = nextChild;
        }
        return null;
    }
	
	protected static List<String> getItemProperties(NodeList p_nodelist)
	{
		List<String> retVal = new ArrayList<String>();
		for (int i=0; i<p_nodelist.getLength(); i++)
		{
			Node nodeVal = p_nodelist.item(i);
			//get ref node to get property name
			Node refNode = getChildrenByNodeName(nodeVal, "ref");
			if (refNode != null)
			{
				String propName = refNode.getTextContent();
				if (propName.startsWith("item"))
					retVal.add(propName);
			}
		}
		
		return retVal;
	}
	
	protected void removeItemProperties(NodeList p_beforeProperties, List<String> p_propertiesOnlyInBeforeUpdate)
	{
		String methodName = "removeItemProperties";
		
		for (int i=0; i<p_beforeProperties.getLength(); i++)
		{
			Node nodeVal = p_beforeProperties.item(i);
			//get ref node to get property name
			Node refNode = getChildrenByNodeName(nodeVal, "ref");
			if (refNode != null)
			{
				String propName = refNode.getTextContent();
				if (p_propertiesOnlyInBeforeUpdate.contains(propName))
				{
					nodeVal.getParentNode().removeChild(nodeVal);
					if (LOG.isTraceEnabled()) {
						LOG.trace(CLASS_NAME + "." + methodName + " Removed node for propName : " + propName);
					}
				}
			}
		}
	}
	
	protected void addItemProperties(Document p_rootDoc, NodeList p_afterProperties, List<String> p_propertiesOnlyInAfterUpdate)
	{
		String methodName = "addItemProperties";
		
		NodeList tyeNodelist = p_rootDoc.getElementsByTagName("type");
		Node typeNode = null;
		
		if (tyeNodelist != null && tyeNodelist.getLength() > 0)
			typeNode = p_rootDoc.getElementsByTagName("type").item(0);
		
		if (typeNode == null)
		{
			LOG.error(CLASS_NAME + "." + methodName + " No type node");
			return;
		}
		
		for (int i=0; i<p_afterProperties.getLength(); i++)
		{
			Node nodeVal = p_afterProperties.item(i);
			Node refNode = getChildrenByNodeName(nodeVal, "ref");
			if (refNode != null)
			{
				String propName = refNode.getTextContent();

				if (p_propertiesOnlyInAfterUpdate.contains(propName))
				{
					Node addNode = p_rootDoc.importNode(refNode.getParentNode(),true);
					typeNode.appendChild(addNode);
					if (LOG.isTraceEnabled()) {
						LOG.trace(CLASS_NAME + "." + methodName + " Added node for propName : " + propName);
					}
				}
			}
		}
	}
	
	protected void updateItemProperties(NodeList p_beforeProperties, NodeList p_afterProperties, List<String> p_propertiesInBeforeAndAfterUpdate)
	{
		String methodName = "updateItemProperties";
		
		for (int i=0; i<p_afterProperties.getLength(); i++)
		{
			Node nodeVal = p_afterProperties.item(i);
			Node refNode = getChildrenByNodeName(nodeVal, "ref");
			Node labelNode = getChildrenByNodeName(nodeVal, "label");
			if (refNode != null)
			{
				String propName = refNode.getTextContent();

				if (p_propertiesInBeforeAndAfterUpdate.contains(propName))
				{
					for (int j=0; j<p_beforeProperties.getLength(); j++)
					{
						Node beforenodeVal = p_beforeProperties.item(j);
						Node beforerefNode = getChildrenByNodeName(beforenodeVal, "ref");
						Node beforelabelNode = getChildrenByNodeName(beforenodeVal, "label");
						if (beforerefNode != null)
						{
							String beforepropName = beforerefNode.getTextContent();
							if (beforepropName.compareToIgnoreCase(propName) == 0)
							{
								//copy label from update properties
								if (labelNode != null && beforelabelNode != null)
								{
									beforelabelNode.setTextContent(labelNode.getTextContent());
									if (LOG.isTraceEnabled()) {
										LOG.trace(CLASS_NAME + "." + methodName + " Updated label for propName : " + beforepropName+" to "+beforelabelNode.getTextContent());
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	protected ActionForward doExecuteDELETE(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForDELETE(request);
			doDelete(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}
	
	private void doDelete(Bean bean, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String methodName = "doDelete";
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}
		// this call will delete the profile types customization from both Highway and the ProfilesTypesCache
		boolean success = ProfilesTypesCache.getInstance().deleteProfileType(bean.orgId);
		//
		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + (success ? "success" : "failed") + " : exiting");
		}
	}

	public ActionForward doExecuteGET(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception	{
		// need to revisit this code for MT. admin privs are enforced by web.xml 
		// and AppContextFilter should set up the admin context as needed.
		// the other issue here is the user could be an org-admin.
		AssertionUtils.assertTrue(AppContextAccess.isUserAnAdmin(), AssertionType.UNAUTHORIZED_ACTION);

		checkMTTestOverride(request); // testing MT on ST env

		// tenantConfig.do API is only allowed in SC / MT environment
		if (_multiTenantConfigEnabled) {
			Bean bean = getBeanForGET(request);

			// if any input data is found to be bad, an API exception will be thrown
			checkRequestData(request, response, bean);

			if (bean.isAuthRequest) {
				response.setDateHeader("Expires", 0);
				response.setHeader("Cache-Control", "no-store,no-cache,must-revalidate");
			}

			response.setContentType(PROFILE_TYPE_CONTENT_TYPE);
			response.setCharacterEncoding(AtomConstants.XML_ENCODING);

			doGet(bean, request, response);
		}
		else {
			throw new APIException(ECause.INVALID_OPERATION);
		}
		return null;
	}

	private void doGet(Bean bean, HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String methodName = "doGet";

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + " : entering");
		}

		boolean success = true;
		// get the profile type definition for the MT org
		String lookupId = ProfileTypeConfig.BASE_TYPE_ID; // "snx:person"
		if (_multiTenantConfigEnabled || _isMTOverride) {
			lookupId = ProfileTypeConfig.MT_BASE_TYPE_ID; // "snx:mtperson"
		}
		ProfileType pt = ProfileTypeHelper.getProfileType(lookupId, bean.orgId);

		if (null != pt) {
			serializeProfileType(pt, response);
		}
		else {
			success = false;
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			String msg = "Internal eror. Highway did NOT return expected ProfileType XML value for org ID : " + bean.orgId;
			if (LOG.isTraceEnabled()) {
				LOG.trace(CLASS_NAME + "." + methodName + " : " + msg);
			}
			throw new APIException(ECause.INVALID_XML_CONTENT);
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace(CLASS_NAME + "." + methodName + (success ? "success" : "failed") + " : exiting");
		}
	}

	private void serializeProfileType(ProfileType profileType, ServletResponse response) throws IOException
	{
		StreamWriter sw = writerFactory.newStreamWriter();
		sw.setWriter(response.getWriter());
		ProfileTypeHelper.serializeProfileType(profileType, sw);
	}

	private String checkRequestData(HttpServletRequest request, HttpServletResponse response, Bean bean) throws APIException {
		String orgId = bean.orgId;
		if (null == orgId)
			throw new APIException(ECause.INVALID_REQUEST);
		else {
			// verify that the supplied orgId maps to a valid org in the db
			Tenant org = _tdiProfileSvc.getTenantByExid(orgId);
			if (null == org) {
				LOG.warn(CLASS_NAME + ".checkRequestData" + " invalid orgId on ProfileType update request. This org id is not recognized by this data set: "+bean.orgId);
				returnErrorMsgToClient(request, response, "error.atomInvalidAPIParameter", orgId);
				throw new APIException(ECause.INVALID_REQUEST);
			}
		}
		return orgId;
	}

	private void returnErrorMsgToClient(HttpServletRequest request, HttpServletResponse response, String msgKey, String msgParam) {
		helper = new ResourceBundleHelper(ResourceManager.getBundle(request));
		String msg = null;
		if (StringUtils.isEmpty(msgParam))
			msg = helper.getString(msgKey);
		else
			msg = helper.getString(msgKey, msgParam);
		try {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
		}
		catch (IOException ex) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(CLASS_NAME + ".returnErrorMsgToClient" + " : " + ex.getLocalizedMessage());
			}
			if (LOG.isTraceEnabled())
				ex.printStackTrace();
		}
	}

	private boolean validateProfileTypes(ProfileType pt1, ProfileType pt2) {
		boolean  isTheSame = true;
		if (!(pt1.getId().equals(pt2.getId())))
			isTheSame = false;
		if (!(pt1.getParentId().equals(pt2.getParentId())))
			isTheSame = false;
		int pt1PropCount = pt1.getProperties().size();
		int pt2PropCount = pt2.getProperties().size();
		if (pt1PropCount != pt2PropCount)
			isTheSame = false;
		return isTheSame;
	}

	private void checkMTTestOverride(HttpServletRequest request) {
		if (getRequestParamBoolean(request, MT_OVERRIDE, false)) {
			_multiTenantConfigEnabled = true;
			_isMTOverride = true;
		}
		if (getRequestParamBoolean(request, HTTP_TEST, false)) {
			_isTesting = true;
		}
	}
	
}

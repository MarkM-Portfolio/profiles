/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.test.policy;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import org.apache.abdera.Abdera;
import org.apache.abdera.writer.StreamWriter;
import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.data.Tenant;
import com.ibm.lconn.profiles.internal.policy.LookupKey;
import com.ibm.lconn.profiles.internal.policy.OrgPolicy;
import com.ibm.lconn.profiles.internal.policy.Permission;
import com.ibm.lconn.profiles.internal.policy.PolicyConfig;
import com.ibm.lconn.profiles.internal.policy.PolicyHolder;
import com.ibm.lconn.profiles.internal.policy.PolicyParser;
import com.ibm.lconn.profiles.internal.policy.XOrgPolicy;
import com.ibm.lconn.profiles.policy.Scope;
import com.ibm.lconn.profiles.test.BaseTestCase;

public class SeralizePolicyTest extends BaseTestCase {
	
	// ensure that delimeters are not allowed for names
	public void testDelimeterNames(){
		// names with the LookupKey delimeter are not allowed
		// <?xml version=\"1.0\"?>
		// <config xmlns="http://www.ibm.com/profiles-policy" id="profiles-policy">
		//   <features>
		//     <feature name="profile.activitystream">
		//       <profileType identity="standard" actorIdentity="standard" mode="internal" actorMode="internal" type="default" actorType="default" enabled="true">
		//         <acl name="profile.activitystream.targetted.event" scope="person_and_self" />
		//       </profileType>
		//     </feature>
		//   </features>
		// </config>
		String baseXml = "<?xml version=\"1.0\"?><config xmlns=\"http://www.ibm.com/profiles-policy\" id=\"profiles-policy\"><features><feature name=\"profile.activitystream\"><profileType identity=\"standard\" actorIdentity=\"standard\" mode=\"internal\" actorMode=\"internal\" type=\"default\" actorType=\"default\" enabled=\"true\"><acl name=\"profile.activitystream.targetted.event\" scope=\"person_and_self\"/></profileType></feature></features></config>";
		boolean result = parseIt(baseXml);
		assertTrue(result);
		//
		String newXml = baseXml.replace("identity=\"standard","identity=\"sta"+LookupKey.delim+"ndard");
		result = parseIt(newXml);
		assertFalse(result);
		//
		newXml = baseXml.replace("actorIdentity=\"standard","actorIdentity=\"sta"+LookupKey.delim+"ndard");
		result = parseIt(newXml);
		assertFalse(result);
		//
		newXml = baseXml.replace("mode=\"internal","mode=\"garbage");
		result = parseIt(newXml);
		assertFalse(result);
		//
		newXml = baseXml.replace("actorMode=\"internal","actorMode=\"garbage");
		result = parseIt(newXml);
		assertFalse(result);
		//
		newXml = baseXml.replace("type=\"default","type=\"some"+LookupKey.delim+"thing");
		result = parseIt(newXml);
		assertFalse(result);
		//
		newXml = baseXml.replace("actorType=\"default","actorType=\"some"+LookupKey.delim+"thing");
		result = parseIt(newXml);
		assertFalse(result);
	}
	private boolean parseIt(String xmlString){
		boolean rtn = true;
		try{
			OrgPolicy op = new OrgPolicy(Tenant.SINGLETENANT_KEY);
			PolicyParser.parsePolicy(xmlString,Tenant.SINGLETENANT_KEY,true,op);
			rtn = op.getLookupKeys().size() > 0;
		}
		catch(Exception ex){
			rtn = false;
		}
		return rtn;
	}
	
	// test that unstated identity specifications resolve as expected
	// specifically, in this specification we do not declare the identity, mode, or type.
	// <?xml version=\"1.0\"?>
	// <config xmlns="http://www.ibm.com/profiles-policy" id="profiles-policy">
	//   <features>
	//     <feature name="profile.activitystream">
	//       <profileType enabled="true">
	//         <acl name="profile.activitystream.targetted.event" scope="person_and_self" />
	//       </profileType>   
	//     </feature>
	//   </features>
	// </config>
	// this declaration should be equivalent
	// <?xml version=\"1.0\"?>
	// <config xmlns="http://www.ibm.com/profiles-policy" id="profiles-policy">
	//   <features>
	//     <feature name="profile.activitystream">
	//       <profileType identity="standard" actorIdentity="standard" mode="internal" actorMode="internal" type="default" actorType="default" enabled="true">
	//         <acl name="profile.activitystream.targetted.event" scope="person_and_self" />
	//       </profileType>
	//     </feature>
	//   </features>
	// </config>
	public void testDefaultSpecifications(){
		String xmlOne = "<?xml version=\"1.0\"?><config xmlns=\"http://www.ibm.com/profiles-policy\" id=\"profiles-policy\"><features><feature name=\"profile.activitystream\"><profileType enabled=\"true\"><acl name=\"profile.activitystream.targetted.event\" scope=\"person_and_self\"/></profileType></feature></features></config>";
		String xmlTwo = "<?xml version=\"1.0\"?><config xmlns=\"http://www.ibm.com/profiles-policy\" id=\"profiles-policy\"><features><feature name=\"profile.activitystream\"><profileType identity=\"standard\" actorIdentity=\"standard\" mode=\"internal\" actorMode=\"internal\" type=\"default\" actorType=\"default\" enabled=\"true\"><acl name=\"profile.activitystream.targetted.event\" scope=\"person_and_self\"/></profileType></feature></features></config>";
		OrgPolicy opOne = new OrgPolicy(Tenant.SINGLETENANT_KEY);
		PolicyParser.parsePolicy(xmlOne,Tenant.SINGLETENANT_KEY,true,opOne);
		OrgPolicy opTwo = new OrgPolicy(Tenant.SINGLETENANT_KEY);
		PolicyParser.parsePolicy(xmlTwo,Tenant.SINGLETENANT_KEY,true,opTwo);
		//
		boolean areEqual = compare(opOne,opTwo);
		assertTrue("test default specifications failed",areEqual);
	}
	
	// i'm all ears for a good test. we have a default orgPolicy (org a). get that OrgPolicy
	// and serialize its representation to XML.
	// we could compare that XML representation - to what?
	// this test takes this XML representation and serializes it back into an OrgPolicy.
	// the resulting OrgPolicy objects are compared.
	public void testXMLSerialization(){	
		try{
			OrgPolicy opa = PolicyHolder.instance().getOrgPolicy(Tenant.SINGLETENANT_KEY);
			assertTrue(opa != null);
			XOrgPolicy xpp = new XOrgPolicy(opa);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Abdera abdera = Abdera.getInstance();
			StreamWriter sw = abdera.newStreamWriter().setOutputStream(baos);
			xpp.serializeToXml(sw);
			//
			String outString = baos.toString("UTF-8");
			System.out.println(outString);
			//
			OrgPolicy newopa = new OrgPolicy(Tenant.SINGLETENANT_KEY);
			PolicyParser.parsePolicy(outString,Tenant.SINGLETENANT_KEY,true,newopa);
			//
			boolean areEqual = compareLookupKeys(opa,newopa);
			assertTrue("key sets are not equal",areEqual);
			areEqual = compare(opa,newopa);
			assertTrue("permisssions are not equal",areEqual);
		}
		catch (Exception ex) {
			assertTrue(false);
		}
		finally{
			LCConfig.instance().revert();
			PolicyConfig.instance().initialize();
		}
	}
	
	private boolean compare(OrgPolicy one, OrgPolicy two){
		boolean rtn = compareLookupKeys(one,two);
		Set<LookupKey> oneKeys = one.getLookupKeys();
		// Permissions should match
		Permission perOne, perTwo;
		Scope scopeOne, scopeTwo;
		for (LookupKey lk : oneKeys){
			perOne = one.getPermission(lk);
			perTwo = two.getPermission(lk);
			if (perOne != null || perTwo != null){
				scopeOne = perOne.getScope();
				scopeTwo = perTwo.getScope();
				rtn = scopeOne.equals(scopeTwo);
			}
			else{
				rtn = false; // shouldn't have null permissions
			}
			if (rtn == false){
				break;
			}
		}
		return rtn;
	}
	
	private boolean compareLookupKeys(OrgPolicy one, OrgPolicy two) {
		boolean rtn = true;
		Set<LookupKey> oneKeys = one.getLookupKeys();
		Set<LookupKey> twoKeys = one.getLookupKeys();
		rtn = (oneKeys.size() > 0 && oneKeys.size() == twoKeys.size());
		if (rtn == true) {
			for (LookupKey lk : oneKeys) {
				rtn = twoKeys.contains(lk);
				if (rtn == false) {
					break;
				}
			}
		}
		return rtn;
	}
}
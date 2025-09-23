/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2014, 2016                                    */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.api.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.config.LCConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig;
import com.ibm.lconn.profiles.config.PropertiesConfig.ConfigProperty;
import com.ibm.lconn.profiles.internal.exception.AssertionType;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.OrgStructureService;
import com.ibm.lconn.profiles.internal.util.AssertionUtils;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileLookupKeySet;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.data.SearchResultsPage;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;

/**
 * An API end-point that will take a list of profile id's and
 * return a flat list of all the profiles that connect them
 * organizationally
 *
 */
public class ProfileFullOrgRelationshipAction extends ProfileAPIAction
{
	private static Logger LOGGER = Logger.getLogger(ProfileFullOrgRelationshipAction.class.getName());
		
	/**
	 * restrict the depth (note the depth of a tree can be done in parallel, if a higher 
	 * node is on the passed in profile list)
	 */
	private static final int MAX_TREE_DEPTH = 10;
	/**
	 * restrict the amount of nodes, secondary restriction to
	 * ensure this feature does not consume resources. Stop at X amount of profiles
	 */
	private static final int MAX_TREE_NODE_SIZE = 100;
	/**
	 * The upper limit on the number of valid inputted Profiles users we will process a tree for
	 */
	private static final int MAX_INPUT_VALID_USERS = 30;
	/**
	 * The upper limit on inputted userIds passed in
	 */
	private static final int MAX_INPUT_USERS = 100;
	
	private static boolean BOTTOM_UP;
	/**
	 * Param to instruct whether we got to top of org or stop
	 * at lowest common ancestor
	 */
	private static String TRAVERSE_TOP = "traverseTop";
	/**
	 * Param to control the max we will process for an org chart
	 * will allow 1 - MAX_INPUT_VALID_USERS
	 * Gives consumers control of the tree sizes within set boundaries
	 */
	private static String MAX_ORG_INPUT_USERS = "maxOrgInputUsers";
	
	private static final String IS_MANAGER_FALSE = "false";

	private static final String IS_MANAGER_TRUE = "true";		

	private OrgStructureService orgStructSvc;
	
	public ProfileFullOrgRelationshipAction() 
	{
		this.orgStructSvc = AppServiceContextAccess.getContextObject(OrgStructureService.class);
		BOTTOM_UP = PropertiesConfig.getBoolean(ConfigProperty.REPORTS_TO_CHAIN_BOTTOM_UP_SORTING);
	}
	
	private static final class Bean extends BaseBean
	{
		public Bean() {}
		public ProfileLookupKeySet plk;
	}
	
	protected BaseBean instantiateActionBean_delegate(HttpServletRequest request) throws Exception 
	{
		long start=0;
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("instantiateActionBean_delegate: "+request);
			start = new Date().getTime();
		}
		
		Bean bean = new Bean();
		bean.searchType = PeoplePagesServiceConstants.REPORTING_CHAIN;
		bean.allowOverrideIsLite = false;
		bean.pageSize = resolvePageSize(request, -1);
		bean.lastMod = new Date().getTime();
		bean.plk = getProfileLookupKeySet(request);
		
		AssertionUtils.assertNotNull(bean.plk);
		String[] plkValues = bean.plk.getValues();
		int numPLKValues = plkValues.length;
		//hard coded limit on input numbers for perf - consider making configurable
		AssertionUtils.assertTrue(numPLKValues <=  MAX_INPUT_USERS);			
		
		boolean traverseTop = getRequestParamBoolean(request, TRAVERSE_TOP, false);
		int maxOrgInputUsers = getRequestParamInt(request, MAX_ORG_INPUT_USERS, 10);
		//hard limits on min and max of amount of users we process an org tree for
		AssertionUtils.assertTrue((maxOrgInputUsers <=  MAX_INPUT_VALID_USERS) && (maxOrgInputUsers > 0));
		bean.resultsPage = buildProfileGraph(bean, traverseTop, maxOrgInputUsers, numPLKValues);		
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("instantiateActionBean_delegate exit: "+bean);			
			long duration = (new Date().getTime() - start);
			LOGGER.finer("instantiateActionBean_delegate exit timer (MS): "+duration);
		}
		return bean;
	}
	
	/**
	 * Implement the post handler as a means to overcome IE limitation on URL length
	 * We may recieve a large amount of input keys so require the option to allow a consumer
	 * post in the key field as a header.
	 * In this case forward the header value into a parameter and forward the request to
	 * standard get handler
	 */
	protected ActionForward doExecutePOST(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {						
		return this.doExecuteGET(mapping, form, request, response);	
	}

// see rtc 175764. if we validate specific content-type in APIAction, this class requires an override
//    /**
//     * Validate content type for a POST request. Derived action classes can override thjis validator.
//     */
//    protected boolean validateContentTypePOST(HttpServletRequest request){
//    	boolean rtn = true;
//    	//String requestUri = request.getRequestURI();
//    	//if (StringUtils.contains(requestUri,"/atom")){
//    		String ct = request.getHeader(HttpHeaders.CONTENT_TYPE); // do we have constants for headers?
//    		rtn = StringUtils.startsWithIgnoreCase(ct,MediaType.APPLICATION_FORM_URLENCODED);
//    	//}
//    	return rtn;
//    }
		
	/**
	 * Build a profile org list from the passed in the list of user identifiers.
	 * This function will return a list of employees that consitute a connected org
	 * tree, going back to the top node in the report-to chain. We return a flat
	 * list, it is up to the consumer to build the tree
	 *   
	 * @param bean - request bean
	 * @param profileCache - profilesCache which holds the cache of the org
	 * @param traverseTop - go to top of org or stop at lowest common ancestor
	 * @param maxOrgUsers - the amount of valid employees we will process and org tree for
	 */
	private SearchResultsPage<Employee> buildProfileGraph(Bean bean,  boolean traverseTop, int maxOrgUsers, int numPLKValues){
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("buildProfileGraph: traverseTop: "+traverseTop);
		}
		SearchResultsPage<Employee> results = null;
		HashMap<String, Employee> profileCache = new HashMap<String, Employee>();
		List<Employee> profiles = null;
		boolean isTreeBuilt = true;
		//if one user passed in and traverseTop=true then shortcut to get a full report chain from the orgService
		if (numPLKValues == 1 && traverseTop == true){
			ProfileLookupKey plk = new ProfileLookupKey(bean.plk.getType(), bean.plk.getValues()[0]);
			profiles = orgStructSvc.getReportToChain(plk, ProfileRetrievalOptions.LITE, BOTTOM_UP, bean.pageSize);
		}
		else {				
			profiles = pps.getProfiles(bean.plk, ProfileRetrievalOptions.LITE);
			if(profiles != null && profiles.size() <= maxOrgUsers){
				List<String> selectedProfilesById = this.getSelectedProfilesById(profiles);			
				HashMap<String, TreeNode> treeCacheHolder = new HashMap<String, TreeNode>();	
				Set<String>  managerSet = cacheProfilesAndGetManagers(profileCache, treeCacheHolder, profiles,  traverseTop);
				buildManagerHierarchy(selectedProfilesById, managerSet, profileCache, treeCacheHolder, bean, traverseTop);
				profiles = new ArrayList<Employee>(profileCache.values());
			} else { // set a flag so that we can override the totalResults - no tree is built we just return the valid profiles passed in.
					// this allows a consumer to render larger data sets in a different way using profiles data
				isTreeBuilt = false;
			}
		}		
						
		int numProfiles = (isTreeBuilt)? profiles.size() : 0;
	    results = new SearchResultsPage<Employee>(profiles, numProfiles, 1, bean.pageSize);				
	    
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("buildProfileGraph: exit: "+profileCache.values());
		}
		return results;		
	}
	
	/**
	 * Return a list of employee ids of the passed in list of Employee objects
	 * @param selectedProfiles
	 * @return
	 */
	private List<String> getSelectedProfilesById(List<Employee> selectedProfiles){
		List<String> selectedProfilesById = new ArrayList<String>();
		for (Employee emp : selectedProfiles) {
			selectedProfilesById.add(emp.getUid());
		}	
		return selectedProfilesById;
	}
	
	/**
	 * Take a cache map and set of Manager ID's and expand the managers if not existing in the cache.
	 * Process until we get to the top of the tree (no manager ids left or we hit some predefined hard limits
	 * such as max_depth)
	 * 
	 * Note this algorithm traverses the tree breadth first from passed in nodes up to the top node.
	 * This ensures that we make only N calls, N being the longest path from a sub-node to parent-node
	 * without any interconnecting node. It accounts for disconnected trees as they are built in parallel.
	 * 
	 * @param selectedProfiles - - the list of users that were passed in and resolved as valid profiles
	 * @param managerSet - List of initial managers of selectedProfiles
	 * @param profileCache - Overall cache of the profile in the org
	 * @param treeCache - Cache of treeNodes holding the tree structure
	 * @param bean - Request bean
	 * @param traverseTop - Request parameter, true go to top of org, false stop at the Lowest common ancestor
	 */
	private void buildManagerHierarchy(List<String> selectedProfiles, Set<String> managerSet, 
			HashMap<String, Employee> profileCache,  HashMap<String, TreeNode> treeCache,  Bean bean, Boolean traverseTop){		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("buildManagerHierarchy: managerList: "+managerSet+", cache: "+profileCache);
		}
		int depthCount = 0;
		//Only one valid user to process, if traverseTop=false then get immediate parent
		boolean singleValidUser = selectedProfiles.size() == 1;
		String managerId = null;
		while((managerSet != null && managerSet.size() > 0) && (depthCount < MAX_TREE_DEPTH) && (profileCache.values().size() < MAX_TREE_NODE_SIZE)){
			//remove already retrieved profiles, any profile we have in cache
			//will already have its chain built in parallel
			Iterator<String> it = managerSet.iterator();    
		    while(it.hasNext()) {
		    	String manager = (String)it.next();
				if(profileCache.containsKey(manager)){
					it.remove();
				} else {
					managerId = manager;
				}
		    }  			
			
		    int mgrSetSize = managerSet.size();
		 
		    if(mgrSetSize > 1 || (singleValidUser && !traverseTop)){
		    	//build the lookup key
				String[] managerArr = managerSet.toArray(new String[mgrSetSize]);
				ProfileLookupKeySet plk = new ProfileLookupKeySet(ProfileLookupKey.Type.UID, managerArr);
				//get expanded manager profiles
				List<Employee> managerProfiles = pps.getProfiles(plk, ProfileRetrievalOptions.LITE);
				
				managerSet = cacheProfilesAndGetManagers(profileCache, treeCache, managerProfiles, traverseTop);	
				//break out if this is a single user scenario - get manager and return
				if(singleValidUser){
					break;
				}
		    } else if(mgrSetSize == 1) {	
		    	managerId = managerSet.iterator().next();
		    	//if there is only one manager left, shortcut to report-to-chain
		    	List<Employee> reportChain = expandReportToChain(managerId, bean);
		    	managerSet = cacheProfilesAndGetManagers(profileCache, treeCache, reportChain, traverseTop);		    	
		    	managerId = getTopManagerFromReportChain(managerId, reportChain);		    		    			    			    		    			   
		    	break;
		    }
		    depthCount = depthCount++;
		}
		
		if(!traverseTop && !singleValidUser){    		
			navigateToTopAndFindAncestor(selectedProfiles, profileCache, treeCache, managerId);				
		}	
		populateValidIsManagerData(treeCache, profileCache);
		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("buildManagerHierarchy exit");
		}
				
	}
	
	/**
	 * Iterate through the tree and populate correct isManager data on employees
	 * @param treeCache
	 * @param profileCache
	 */
	private void populateValidIsManagerData(HashMap<String, TreeNode> treeCache, HashMap<String, Employee> profileCache){
		for (String treeEntry : treeCache.keySet()) {
			TreeNode node = treeCache.get(treeEntry);
			Employee emp = profileCache.get(treeEntry);
			if(emp != null){
				if(node.getChildren() != null && node.getChildren().size() > 0){				
					emp.setIsManager(IS_MANAGER_TRUE);
				} else {
					emp.setIsManager(IS_MANAGER_FALSE);
				}
			}			
		}
	}

	private String getTopManagerFromReportChain(String managerId, List<Employee> reportChain) {
		if(reportChain != null && reportChain.size() > 0){
			Employee topEmployee = reportChain.get(reportChain.size()-1);
			managerId = topEmployee.getUid();		    		
		}
		return managerId;
	}
	
	/**
	 * Our breadth first traversal can mean that we climb higher than the common ancestor
	 * This will run the tree from top down removing the nodes until we get to the lowest common ancestor
	 * LCA can be of two types
	 * 1) LCA is common intersector between passed in nodes (hence will have >1 children)
	 * 2) LCA is a passed in node ( one of the passed in profiles ) can have 1 child
	 *  
	 * @param selectedProfiles - The users passed in via url parameter
	 * @param profileCache - Overall cache of employees.
	 * @param treeCache - A cache of treeNodes giving us the tree structure built
	 * @param managerProfile - the Last profile we 
	 */
	private void navigateToTopAndFindAncestor(List<String> selectedProfiles, HashMap<String, Employee> profileCache, 
			HashMap<String, TreeNode> treeCache, String manager){
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("navigateToTopAndFindAncestor: selectedProfiles: "+selectedProfiles+", managers: "+manager+", treeCache: "+treeCache);
		}

		TreeNode topNode = treeCache.get(manager);
		if(topNode != null){
			topNode = navigateToTop(topNode);			
			//work down to Lowest Common Ancestor
			topNode = removeTopNodes(selectedProfiles, profileCache, treeCache, topNode);
		}
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("navigateToTopAndFindAncestor exit"+topNode);
		}
	}
	
	/**
	 * Find the top treenode
	 * 
	 * @param topNode
	 * @return
	 */
	private TreeNode navigateToTop(TreeNode topNode) {
		int count = 0;
		//navigate to top of tree
		while(topNode.getParent() != null && count < 30){
			//sanity check circular ref
			if(topNode.getParent().getId() == topNode.getId()){
				if (LOGGER.isLoggable(Level.FINER)) {
					LOGGER.finer("navigateToTop: circular ref found"+ topNode.getParent().getId());
				}
				break;
			}
			topNode = topNode.getParent();
			count++;
		}
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("navigateToTop: top node: "+ topNode.getId());
		}
		return topNode;
	}
	
	/**
	 * Iterate from top down removing non LCA (lowest common ancestor nodes)
	 * 
	 * @param selectedProfiles
	 * @param profileCache
	 * @param topNode
	 */
	private TreeNode removeTopNodes(List<String> selectedProfiles, HashMap<String, Employee> profileCache, 
			HashMap<String, TreeNode> treeCache, TreeNode topNode) {
		int count = 0;
		while(topNode.getChildren().size() == 1 && count < 10){
			Set<TreeNode> children = topNode.getChildren();
			//Scenario where the LCA is a passed in ID - main node not an intersector
			if(selectedProfiles.contains(topNode.getId())){				
				break;
			}
			profileCache.remove(topNode.getId());
			treeCache.remove(topNode.getId());
			topNode = children.iterator().next();
			count ++;
		}
		return topNode;
	}
	
	/**
	 * Take a profileID and return the report-to-chain
	 */
	private List<Employee> expandReportToChain(String profileId, Bean bean) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("expandReportToChain: "+profileId);
		}
		ProfileLookupKey plk = new ProfileLookupKey(ProfileLookupKey.Type.UID, profileId);
		return orgStructSvc.getReportToChain(plk, ProfileRetrievalOptions.LITE, BOTTOM_UP, bean.pageSize);
	}
	
	/**
	 * iterate through a list of Employee's, cache them and return the manager set 
	 */
	private Set<String> cacheProfilesAndGetManagers(HashMap<String, Employee> profileCache, 
			HashMap<String, TreeNode> treeCache, List<Employee> profiles, boolean traverseTop) {
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("cacheProfilesAndGetManagers: profiles: "+profiles+", cache: "+profileCache);
		}
		
		Set<String> managerSet = new HashSet<String>();
		for(Employee emp : profiles ) {				
			String empUid = emp.getUid();
			profileCache.put(empUid, emp);			
			String mgrUid = emp.getManagerUid(); //perf. get value from HashMap once
			if (null != mgrUid && !mgrUid.trim().equals("")){
				managerSet.add(mgrUid);	
			}
					
			TreeNode mgrNode = this.processTreeNode(mgrUid, treeCache);				
			TreeNode empNode = this.processTreeNode(empUid, treeCache);
			if(mgrNode != null){
				mgrNode.addChild(empNode);					
			}			
									
		}		
		if (LOGGER.isLoggable(Level.FINER)) {
			LOGGER.finer("cacheProfilesAndGetManagers exit: managerList: "+managerSet);
		}
		return managerSet;
	}
	
	private TreeNode processTreeNode(String uid, HashMap<String, TreeNode> treeCache){
		TreeNode mgrNode = null;
		if(uid != null){
			mgrNode = treeCache.get(uid);
			if(mgrNode == null){
				mgrNode = new TreeNode(uid);	
				treeCache.put(uid, mgrNode);		
			}	
		}		
		return mgrNode;
	}
	
	/**
	 * Simple TreeNode representation, used to track the org tree structure
	 * for later processing
	 */
	private class TreeNode {
	    String id;
	    TreeNode parent;
	    Set<TreeNode> children;

	    public TreeNode(String data) {
	        this.id = data;
	        this.children = new HashSet<TreeNode>();
	    }

	    public void addChild(TreeNode childNode) {
	    	if(childNode != null){
		    	childNode.setParent(this);
		        this.children.add(childNode);		    		
	    	}
	    }
	    
	    public Set<TreeNode> getChildren(){
	    	return this.children;
	    }
	    
	    public String getId(){
	    	return this.id;
	    }
	    
	    public TreeNode getParent(){
	    	return this.parent;
	    }
	    
	    public void setParent(TreeNode parentNode){
	    	this.parent = parentNode;
	    }
	    
	    public boolean equals(Object other){
	    	return other.equals(id);
	    }	
	    
	    public String toString(){
	    	return this.id;
	    }
	}
}

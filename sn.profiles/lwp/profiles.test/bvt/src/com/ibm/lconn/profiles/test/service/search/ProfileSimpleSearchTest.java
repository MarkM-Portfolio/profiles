/* ***************************************************************** */
/*                                                                   */
/* HCL Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright HCL Technologies Limited 2006, 2021                     */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */
package com.ibm.lconn.profiles.test.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.ibm.lconn.profiles.config.dm.TagConfig;
import com.ibm.lconn.profiles.data.AbstractName.NameSource;
import com.ibm.lconn.profiles.data.ProfileSetRetrievalOptions;
import com.ibm.lconn.profiles.data.Surname;
import com.ibm.lconn.profiles.data.Tag;
import com.ibm.lconn.profiles.internal.data.profile.UserMode;
import com.ibm.lconn.profiles.internal.data.profile.UserState;
import com.ibm.lconn.profiles.internal.service.AppServiceContextAccess;
import com.ibm.lconn.profiles.internal.service.GivenNameService;
import com.ibm.lconn.profiles.internal.service.ProfileTagService;
import com.ibm.lconn.profiles.internal.service.SearchService2;
import com.ibm.lconn.profiles.internal.service.SurnameService;
import com.ibm.lconn.profiles.internal.util.ProfileNameUtil;
import com.ibm.lconn.profiles.test.BaseTransactionalTestCase;
import com.ibm.lconn.profiles.test.CreateUserUtil;
import com.ibm.peoplepages.data.Employee;
import com.ibm.peoplepages.data.ProfileLookupKey;
import com.ibm.peoplepages.data.ProfileRetrievalOptions;
import com.ibm.peoplepages.internal.service.PeoplePagesServiceConstants;
import com.ibm.peoplepages.service.PeoplePagesService;
import com.ibm.peoplepages.util.appcntx.AppContextAccess;

/*
 *
 */

public class ProfileSimpleSearchTest extends BaseTransactionalTestCase 
{
	private PeoplePagesService pps;
	private SearchService2 searchSvc;
	private ProfileTagService tagSvc;
	private GivenNameService givenNameSvc;
	private SurnameService surnameSvc;
	
	private Employee employeeA, employeeB;
	
	protected void onSetUpBeforeTransactionDelegate() {
		pps = AppServiceContextAccess.getContextObject(PeoplePagesService.class);
		searchSvc = AppServiceContextAccess.getContextObject(SearchService2.class);
		tagSvc = AppServiceContextAccess.getContextObject(ProfileTagService.class);
		givenNameSvc = AppServiceContextAccess.getContextObject(GivenNameService.class);
		surnameSvc = AppServiceContextAccess.getContextObject(SurnameService.class);
	}

	protected void onSetUpInTransaction() throws Exception {
		// create some new profiles
		Map<String,Object> paramA = new HashMap<String,Object>();
		paramA.put(PeoplePagesServiceConstants.JOB_RESPONSIBILITIES, "unit_test_job_title");
		paramA.put(PeoplePagesServiceConstants.PHONE_NUMBER, "unit_test_phone_number");
		paramA.put(PeoplePagesServiceConstants.GROUPWARE_EMAIL, "unit_test_groupware_mail");
		paramA.put(PeoplePagesServiceConstants.PROF_TYPE, "unit_test_profile_type");		
		
		employeeA = CreateUserUtil.createProfile("employeeA","employeeAemail", paramA);
		employeeB = CreateUserUtil.createProfile("employeeB","employeeBemail", paramA);
		runAs(employeeA); // must run as someone
	}
	
	/**
	 *  Perform simple lookup for employee
	 * @throws Exception if an error
	 */
	public void testFindEmployee() throws Exception {
		try {
    		Employee emp = pps.getProfile(ProfileLookupKey.forEmail(employeeA.getEmail()), ProfileRetrievalOptions.MINIMUM);
    		
    		assertEquals(emp.getKey(), employeeA.getKey());
    	}
    	catch(Exception ex){
    		fail("Failed to find employee!");
    	}
	}
	
	public void testNameSearch() throws Exception {
		// mimic code from NameTypeAheadAction to analyze the resulting query
		String name = StringUtils.lowerCase("Bob Smith");
		int entryCount = 20;
		
		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(entryCount);
		
		List<Employee> profiles;
		profiles = searchSvc.findProfilesByName(name, options);
	}
	
	/**
	 * Perform test to check a few non-supported params for database search.
	 */
	public void testSimpleSearchByNonSupportedParams() {
    	Map<String,Object> searchParams = new HashMap<String,Object>();

    	searchParams.put(PeoplePagesServiceConstants.DEPARTMENT, (Object)employeeA.getDepartmentTitle());
    	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
    	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
    	assertTrue(keys.isEmpty());
    	
    	searchParams.clear();
       	searchParams.put(PeoplePagesServiceConstants.EMAIL, (Object)employeeA.getEmail());    	
    	options = new ProfileSetRetrievalOptions(100);
     	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
     	assertTrue(keys.isEmpty());
    	
    	searchParams.clear();
       	searchParams.put(PeoplePagesServiceConstants.JOB_RESPONSIBILITIES, (Object)employeeA.getJobResp());  	
    	options = new ProfileSetRetrievalOptions(100);
     	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
     	assertTrue(keys.isEmpty());

       	searchParams.clear();
       	searchParams.put(PeoplePagesServiceConstants.PHONE_NUMBER, (Object)employeeA.getTelephoneNumber()); 	
    	options = new ProfileSetRetrievalOptions(100);
     	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
     	assertTrue(keys.isEmpty());
    	
       	searchParams.clear();
       	searchParams.put(PeoplePagesServiceConstants.GROUPWARE_EMAIL, (Object)employeeA.getGroupwareEmail()); 	
    	options = new ProfileSetRetrievalOptions(100);
     	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
     	assertTrue(keys.isEmpty());
    	
       	searchParams.clear();
       	searchParams.put(PeoplePagesServiceConstants.PROF_TYPE, (Object)employeeA.getProfileType()); 	
    	options = new ProfileSetRetrievalOptions(100);
     	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
     	assertTrue(keys.isEmpty());
		
	}
	/**
	 *  Perform simple tag search.
	 */
    public void testSimpleTagSearch() {
    	
    	try {
    		//Add some tags to EmployeeA
    		updateTags(employeeA, new String[]{"unit_test_foo", "test_bar"});
    	
    		// Try to perform a tag search. Expect to find employeeA
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object)"unit_test_foo");
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);       	
        	assertTrue( keys.contains(employeeA.getKey()));
    	}
    	catch(Exception ex) {
    		fail("Caught excpetion while doing simpleSearch test: " +ex);
    	}
    }
    
	/**
	 *  Perform simple tag search.
	 */
    public void testCompoundNameSearch() {
    	
    	try {
    	   	// Add first name to employeeA (odd name to try to avoid collision)
    		addFirstName( employeeA, new String[]{"david","david alex"});
    		addLastName(employeeA, new String[]{"jones", "test_last_name_a", "test_last_name_b"});
    	
    		// Now try to find those names
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"david% alex% jones");    	
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
        	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	assertTrue( keys.contains(employeeA.getKey()));
        	
        	// Find by name
        	options = new ProfileSetRetrievalOptions(50);
        	List<Employee> employees = searchSvc.findProfilesByName("david",options);
        	assertTrue( employees.size() > 0);
        	
        	// Find by name
        	options = new ProfileSetRetrievalOptions(50);
        	employees = searchSvc.findProfilesByName("*david*",options);
        	assertTrue( employees.size() > 0);

    	   	// Add names to employeeB
    		addFirstName( employeeB, new String[]{"george","george herbert walker"});
    		addLastName(employeeB, new String[]{"bush"});
    	
    		// Now try to find those names
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"george herbert walker bush");    	
        	options = new ProfileSetRetrievalOptions(100);
        	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	
        	assertTrue( keys.contains(employeeB.getKey()));
    	
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"george bush");    	
        	options = new ProfileSetRetrievalOptions(100);
        	keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	assertTrue( keys.contains(employeeB.getKey()));
    	
    	}
    	catch(Exception ex) {
    		fail("Caught excpetion while doing simpleSearch test: " +ex);
    	}
    }

    /**
     *  Perform simple name search using search service.
     */
    public void test_name_search() {
    	
    	try {
    	   	// Add first name to employeeA
    		addFirstName( employeeA, new String[]{"test_first_name_a","test_first_name_b"});
    		addLastName(employeeA, new String[]{"test_last_name_a", "test_last_name_b"});
    		
    		// Now try to find those names
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"test_first_name% test_last_name%");
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
        	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	
        	assertTrue( keys.contains( employeeA.getKey()) ); 		
    	}
    	catch(Exception ex){
    		fail("caught exception: ex = " +ex);
    	}
    }
    
    /**
     * Test name and tag search
     */
    public void testNameAndTagSearch() {
    	
    	try {
    	   	// Add first name to employeeA
    		addFirstName( employeeA, new String[]{"test_first_name_a","test_first_name_b"});
    		addLastName(employeeA, new String[]{"test_last_name_a", "test_last_name_b"});
 
    		//Add some tags to EmployeeA
    		updateTags(employeeA, new String[]{"unit_test_tag", "test_bar"});
    		
    		// Now try to find those names
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"test_first_name% test_last_name%");
        	searchParams.put(PeoplePagesServiceConstants.PROFILE_TAGS, (Object)"unit_test_tag, test_bar"); 
           	
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	assertTrue( keys.contains( employeeA.getKey()) ); 		
    	}
    	catch(Exception ex){
    		fail("caught exception: ex = " +ex);
    	}
    }
    
    /**
     * Test empty string searches. Should also debug the trace to make sure that these searches
     * do not hit the database at all.
     */
    public void testEmptyValueSearch() {
    	
    	try {
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"");
           	
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	assertTrue( keys.isEmpty() ); 
        	
        	Map<String,Object> searchParams1 = new HashMap<String,Object>();
        	searchParams1.put(PeoplePagesServiceConstants.NAME, (Object)" ");
        	options = new ProfileSetRetrievalOptions(10);
         	List<String> keys1 = searchSvc.dbSearchForProfileKeys(searchParams1, options);
        	assertTrue( keys1.isEmpty() ); 
        	
        	Map<String,Object> searchParams2 = new HashMap<String,Object>();
        	searchParams2.put(PeoplePagesServiceConstants.NAME, (Object)"\r \t \b");
        	options = new ProfileSetRetrievalOptions(10);
         	List<String> keys2 = searchSvc.dbSearchForProfileKeys(searchParams2, options);
        	assertTrue( keys2.isEmpty() );
    	}
    	catch(Exception ex){
    		fail("caught exception: ex = " +ex);
    	}
    }
    
    /**
     * Test the wildcard scrubbing. The main point here is we do not allow preceding wildcards
     * in the simple name searches.
     */
    public void testScubWildcard(){
    	String[] input = {"%%%abc%%%", 
					"%%abc%%",
					"%abc%",
					"abc%%%",
					"abc%%",
					"a%bc%",
					"ab%c",
					"abc",
					"a",
					"%%abc%%",
					"%%abc%%",
					"%%a%%%b%c%%",
					"%",
					"%%",
					""
					};
       	String[] output = {"abc%", 
				"abc%",
				"abc%",
				"abc%",
				"abc%",
				"a%bc%",
				"ab%c%",
				"abc%",
				"a%",
				"abc%",
				"abc%",
				"a%%%b%c%",
				"%",
				"%",
				""
				};
       	String s;
       	for (int i = 0 ; i < input.length ; i++){
       		s = ProfileNameUtil.scrubWildcards(input[i]);
       		//System.out.println("in: "+input[i]+" out: "+s);
       		assertTrue( s.equals(output[i]));
       	}
    			
    }
    
    
    /**
     * Test empty string searches. Should also debug the trace to make sure that these searches
     * do not hit the database at all.
     */
    public void testWildCardValueSearch() {
    	
    	try {
        	Map<String,Object> searchParams = new HashMap<String,Object>();
        	searchParams.put(PeoplePagesServiceConstants.NAME, (Object)"%");
           	
        	ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(searchParams, options);
        	assertTrue( keys.isEmpty() ); 
        	
        	Map<String,Object> searchParams1 = new HashMap<String,Object>();
        	searchParams1.put(PeoplePagesServiceConstants.NAME, (Object)"% % %");
        	options = new ProfileSetRetrievalOptions(10);
         	List<String> keys1 = searchSvc.dbSearchForProfileKeys(searchParams1, options);
        	assertTrue( keys1.isEmpty() ); 
        	
        	Map<String,Object> searchParams2 = new HashMap<String,Object>();
        	searchParams2.put(PeoplePagesServiceConstants.NAME, (Object)"%\r %\t \n");
        	options = new ProfileSetRetrievalOptions(10);
         	List<String> keys2 = searchSvc.dbSearchForProfileKeys(searchParams2, options);
        	assertTrue( keys2.isEmpty() );
        	
        	Map<String,Object> searchParams3 = new HashMap<String,Object>();
        	searchParams3.put(PeoplePagesServiceConstants.NAME, (Object)"%\r% \f  \t\n");
        	options = new ProfileSetRetrievalOptions(10);
         	List<String> keys3 = searchSvc.dbSearchForProfileKeys(searchParams3, options);
        	assertTrue( keys3.isEmpty() );
    	}
    	catch(Exception ex){
    		fail("caught exception: ex = " +ex);
    	}
    }
       
    
    /**
     * ensure that results are unique
     */
    public void test_unique() {
    	List<Surname> sN = surnameSvc.getNames(employeeA.getKey(), NameSource.SourceRepository);
    	
    	assertTrue( sN.size() > 0 );
    	
    	for (char c = 'a'; c <= 'z'; c++) {
    		String firstSurname = sN.get(0).getName();
    		HashMap<String,Object> m = new HashMap<String,Object>();
    		m.put(PeoplePagesServiceConstants.NAME, (Object)firstSurname);
    		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(100);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(m, options);
    		assertEquals(keys.size(), new HashSet<String>(keys).size());
    	}
    }
    
    public void test_bad_input_does_not_blow_up_system() {
    	for (String input : new String[]{",", ", dsf , , ", " , , "}) {
       		HashMap<String,Object> m = new HashMap<String,Object>();
    		m.put(PeoplePagesServiceConstants.NAME, (Object)input);
    		ProfileSetRetrievalOptions options = new ProfileSetRetrievalOptions(10);
         	List<String> keys = searchSvc.dbSearchForProfileKeys(m, options);
    		assertEquals(keys.size(), 0);
    	}
    }
    
    private void updateTags(Employee emp, String[] tags) throws Exception {
    	List<String> tagsList = Arrays.asList(tags);
    	
    	runAs(emp);
    	
    	List<Tag> tagObjects = new ArrayList<Tag>();
    	for (String tag : tags) {
    		Tag aTag = new Tag();
    		aTag.setTag(tag);
    		aTag.setType(TagConfig.DEFAULT_TYPE);
    		tagObjects.add(aTag);
    	}
    	
    	tagSvc.updateProfileTags(emp.getKey(), emp.getKey(), tagObjects, true); 
    	
    	List<Tag> newTagList = tagSvc.getTagsForKey(emp.getKey());
    	
    	System.out.println("updateTags: newTagList = " +newTagList );
    }

    private void addFirstName(Employee emp, String[] names) throws Exception {
    	givenNameSvc.setNames(emp.getKey(), NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, Arrays.asList(names));
    	
    	System.out.println(" Added first names: " +givenNameSvc.getNames(emp.getKey(), NameSource.SourceRepository));
    }
    
    private void addLastName(Employee emp, String[] names) throws Exception {
    	surnameSvc.setNames(emp.getKey(), NameSource.SourceRepository, UserState.ACTIVE, UserMode.INTERNAL, Arrays.asList(names));
    	
    	System.out.println(" Added last names: " +surnameSvc.getNames(emp.getKey(), NameSource.SourceRepository));	
	}
}


The following instructions outline how to do the following:

1) Configure WebSphere Security to use the Internal File Registry
2) Enable Organization and Policy Support
3) Populate the profiles database via internal file registry
4) Generate a user directory for multi-tenant deployments

Configure WebSphere Security to use the Internal File Registry with Organization and Policy Support 

1) Start WebSphere Application Server (WAS)
2) Open WAS Admin Console
3) Click Security -> Global Security
4) Click Web and SIP Security -> Single Sign-on (SSO)
5) Click Enabled, and populate your SSO domain name (i.e. .raleigh.ibm.com)
6) Click Apply and then Save configuration to return to Global Security
7) Under "Available realm definitions", select "Federated Repositories", and click Configure
8) Set primary administrative user name to "wasadmin"
9) Click "Use built in repository"
10) Save changes, and populate administrative password for "wasadmin", return to Global Security
11) Click "Enable Administrative Security" and "Enable Application Security"
12) Click Apply and Save changes
13) Stop WAS
14) Copy wimxmlextension.xml into <wasprofile>/config/cells/<cellName>/wim/model
15) Copy wimconfig.xml into <wasprofile>/config/cells/<cellName>/wim/config/wimconfig.xml 
16) Copy fileRegistry.xml into <wasprofile>/config/cells/<cellName>, overwriting the existing version.
17) Restart WAS.  Verify WAS starts correctly.
18) Open WAS Admin Console and login as wasadmin
19) Click "Users and Groups" and "Manage Users", Search for * and see all configured users

Each user password is "passw0rd".

Enable Organization and Policy Support

1) Stop server.
2) Open <wasprofile>/config/cells/<cellName>/LotusConnections-config/directory.services.xml in a text editor

	Change the default Waltz provider:
	
		<profileProvider class="com.ibm.connections.directory.services.provider.WaltzServiceProvider" />
	
	to:
	
		<profileProvider class="com.ibm.connections.directory.services.provider.WaltzServiceProvider">
			<property name="com.ibm.connections.directory.services.waltz.custom.user.id.attribute.type">uid</property>
			<property name="com.ibm.connections.directory.services.waltz.custom.group.id.attribute.type">cn</property>
			<property name="com.ibm.connections.directory.services.waltz.custom.policy.id.attribute.type">uid</property>
			<property name="com.ibm.connections.directory.services.waltz.custom.organization.id.attribute.type">ibm-saasorganizationid</property>		
			<property name="com.ibm.connections.directory.services.returned.attribute.person">ibm-saasorganizationid</property>
			<property name="com.ibm.connections.directory.services.returned.attribute.person">ibm-saasmultitenancyid</property>
			<property name="com.ibm.connections.directory.services.returned.attribute.person">ibm-saasprimaryorganizationid</property>
			<property name="com.ibm.connections.directory.services.returned.attribute.organization">ibm-vmmorgpolicyid</property>		
			<property name="com.ibm.connections.directory.services.attribute.id.organization">ibm-saasorganizationid</property>
			<property name="com.ibm.connections.directory.services.attribute.name.organization">cn</property>
			<property name="com.ibm.connections.directory.services.attribute.id.policy">uid</property>		
			<property name="com.ibm.connections.directory.services.attribute.name.policy">uid</property>
			<property name="com.ibm.connections.directory.services.returned.attribute.policy">ibm-saashasgroups</property>
			<property name="com.ibm.connections.directory.services.returned.attribute.policy">ibm-saassharingintent</property>		
		</profileProvider>
	
	This instructs Waltz to return these attributes with each user, and to use the provided custom attributes to look up
	people, groups, policies, and organizations (instead of the default VMM external id).
	
3) Restart server.
4) Recreate service database if necessary (i.e. communities, files, wikis, etc.)

Populate the profiles database via internal file registry and photos

1) Open /profiles.test/api/src/com/ibm/lconn/profiles/test/rest/junit/publicApiTest.properties
2) Set baseUrl, baseUrlHttp, adminNoProfileUsername, and adminNoProfilePassword for your deployment
3) Unzip user_images_byuid.zip to a location on disk (i.e. c:\dev\config)
4) Find the location of the fileRegistry.xml file in your Application Server (i.e. <wasprofile>/config/cells/<cellName>/fileRegistry.xml)
5) Open "PopulationTool.java"
6) Click Run -> Run Configurations
7) Click "New" button
8) Click Arguments, and add the following to Program Arguments
	-directory=<path to fileRegistry.xml file> 
	-populate 
	-photo=<path_to_user_imagesby_uid folder>
	-tags
	-reportToChain
	-globalLeadershipData
	-codes
	-w3 <if_you_want_to_populate_w3_extension_fields>
	 
9) Click Run

Your profiles database is now populated with users, and they have photos.	

Note: you can run the command multiple times, or you can choose to just run a particular command by leaving options off (i.e. photo or populate only)

Generate a user directory for multi-tenant deployments

1) Copy /profiles.test/api/populationTool/DirectoryGeneratorArguments.xml to a temp location (i.e. c:\dev\temp)
2) Modify the DirectoryGeneratorArguments.xml to meet your generation needs (number of tenants, users per tenant, etc.)
3) Open "DirectoryGenerator.java"
4) Click Run -> Run Configurations
5) Click "New" button
6) Click Arguments, and add the following to Program Arguments
	-input=c:\dev\temp\DirectoryGeneratorArguments.xml -output=c:\dev\temp\
7) Click Run

The generated directory structure will appear in your output directory.
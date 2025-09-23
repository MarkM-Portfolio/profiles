[![Build Status](https://jenkins.cwp.pnp-hcl.com/cnx/buildStatus/icon?job=Core%2FBlue%2FIC10.0%2FIC10.0_Profiles)](https://jenkins.cwp.pnp-hcl.com/cnx/job/Core/job/Blue/job/IC10.0/job/IC10.0_Profiles/)

# Profiles

This repo contains the Connections server code required to support the Profiles application.  

## Running local builds

This build requires Java 8 using the setupUTLS build room package.

1. Change directory to `sn.profiles/lwp`

2. Verify that you have downloaded the latest prereqs required by the profiles server build.  Download the latest prereqs using the following command.  Note that this is a very large download (1GB+).  Once downloaded, they do not need to be refreshed unless changed.<br/>
`wsbld downloadFEs`

3.  Clean and run the local build.  Direct the output to a file called build.log.<br/>
`wsbld -l build.log clean production`

4.  Even if the overall result is SUCCESS, check the log for errors.  Explicitly search for "error", you should not get any results.  If none, then the build is clean.

## Running unit tests

You must run your local build first, i.e., at the level: sn.profiles/lwp, run the following:</br>
`wsbld -l build.log`

Make sure that build went through successfully. If you encounter some errors, fix them first. Please also check the 'Trouble shooting' section for some common errors. <br/>

To run the Profiles UT, change directory to `sn.profiles/lwp/profiles.test/bvt/`.

Then copy the necessary config files for running tests: <br/>
`wsbld copy-config-files`

The Profiles UT requires access to a DB2 database.  Either deploy your own DB2 instance manually or via a docker image (preferred), or reserve a pool server.

Edit the following two copies of test.properties:</br>

sn.profiles/lwp/profiles.test/test.properties</br>
sn.profiles/lwp/profiles.test/bvt/test.properties</br>

Validate that the following field match your db2 environment. This example is using a pool server: </br>

jdbc_driver=com.ibm.db2.jcc.DB2Driver<br/>
jdbc_url=jdbc:db2://lcauto124.cnx.cwp.pnp-hcl.com:50000/peopledb<br/>
jdbc_username=lcuser<br/>
jdbc_password=ewpuxtmi<br/>

Then run the following:<br/>
<br/>
`wsbld -l bvt.log bvt`

Check the log for test results and errors (even if the overall result is SUCCESS).
The test output can be found in `build/profiles.test/bvt/logs`.

If you need to rerun the test, clear your database data first.  If using the docker container, stop it, clear your data directory it was using, and then restart it.

If you need to make changes to a UT test, make the changes and then recompile in the following directories using `wsbld production` and then rerun the bvt test.

## Trouble shooting

1. If you see the following error or similar error complaining about profiles-policy-cloud.xml, in the test output: </br>

`SEVERE: Unable to initialize Profiles application.  Unable to read configuration file at: file:/Users/joseph_lu/hcl/git/ic/profiles/sn.profiles/lwp/profiles.test/bvt/testconf/profiles-types.xml
Aug 16, 2021 2:03:23 PM com.ibm.lconn.profiles.internal.config.ConfigurationProvider <init>
WARNING: An invalid property ref, labelrw was found in the profiles-types.xml.  The property referenced is not a valid standard or extension field.`

Make sure that you have the latest file: `sn.profiles/lwp/profiles.test/bvt/build.xml`. You can check the folder: `sn.profiles/lwp/profiles.test/bvt/testconf/`  and you are NOT supposed to see any cloud related configuration files, i.e., any files with file pattern like: `*-cloud.xml`

2. _schema.bld does not exist error

`
Caused by: /Users/joseph_lu/tools/UTLS_Tools/scripts/imports/root_build.xml:374: The following error occurred while executing this line:
/Users/joseph_lu/hcl/git/ic/profiles/sn.profiles/lwp/profiles.sn.install/build.xml:337: /Users/joseph_lu/hcl/git/ic/profiles/sn.profiles/lwp/profiles.db/schemas/_schema.bld/gen.schemas/db2 does not exist.
	at org.apache.tools.ant.ProjectHelper.addLocationToBuildException(ProjectHelper.java:575)
	at org.apache.tools.ant.taskdefs.Ant.execute(Ant.java:443)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)`
    
You can fix this error, by going to folder: `sn.profiles/lwp/profiles.db/schemas` and run `wsbld`. After it is done, you should check whether folder: `sn.profiles/lwp/profiles.db/schemas/_schema.bld/`  exists or not.

These tests run the infra version of the policy checking done in pre-5.0 releases.
The policy checking code was moved to infra in the 3.0 timeframe when Activity Streams subsumed the Profile's message board.
At that time, the message board feature and associated policies remained in the profiles-policy.xml file. It remains there
as the board commenting policies were never migrated to the News/Homepage application, which continues to read the 
profiles-policy.xml file. The Profiles policy calculations needed to evolve for the visitor model and the policy calculation
code was moved back into Profiles for the 5.0 release. Profiles continues to apply the profiles-policy.xml settings, but does
not plan to evolve the capabilties tied to profile types.

The tests in this package run the pre-5.0 unit test (Pre50ProfileUserFeatureTest). The version of profiles-policy.xml used for
the tests is copied from the profiles.test/bvt/configadd directory. The tests run with the infra code as (possibly) used by
News/Homepage. Any updates to the tests should continue to run with the infra code to ensure backwards compatibility.

The test Pre50AclDefsCheckTest compares the infra and Profiles versions of the acl definitions. The intent is to ensure that
the infra set remains a subset of the Profiles set. The test compares the AclDefs by name, feature name, default scope
setting, and default read only status. Any differences in these settings will produce a bvt failure.

Again, the point of these tests is to maintain backwards compatibility. Our plan moving forward is to use the profile type
to define what attributes are assigned to a profile. We do not plan to overload the profile type with more policy settings.
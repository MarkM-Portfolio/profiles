The MessageBoardApiTest tests backwards compatibility of 3.0 API's,
which now forward to News/Homepage via news EJB.

This test requires a running news/homepage application, as content is
sent to that application for the Activity Stream. The ejb is pulled
in via the profile.ear component.xml via
    <!-- v4.0 microbloggging -->
    <dependency name="lc.component.integration/news" type="jar" />
	<dependency name="tk.rproxysvc/client" type="jar" />
	<dependency name="tk.rproxysvc/transport" type="jar" />
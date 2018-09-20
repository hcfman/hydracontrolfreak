<h1>System Status</h1>

<div id="system-status"></div>

<jsp:include page="/jsp/content/components/scripts.jsp" />

<!-- Loading action scripts -->
<script type="text/javascript" src="/hcf/js/system-status/templates.js"></script>
<script type="text/javascript" src="/hcf/js/system-status/controller.js"></script>
<script type="text/javascript" src="/hcf/js/system-status/builder.js"></script>

<!-- Start loading the config -->
<script type="text/javascript">
	try {
		ConfigLoadJSON('/hcf/systemstatusgetjson');
	} catch (e) {
		console.error( e );
	}
</script>
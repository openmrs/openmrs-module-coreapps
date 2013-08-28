<div id="most-recent-vitals-container">
    Loading...
</div>

<script type="text/javascript"/>
	if (${encounterId}) {
		emr.getFragmentActionWithCallback("htmlformentryui", "htmlform/viewEncounterWithHtmlForm", "getAsHtml", { encounterId: ${encounterId} }, function(result) {
		    jq('#most-recent-vitals-container').html(result.html);
		});
	}
	else {
		jq('#most-recent-vitals-container').html('');
	}
</script>
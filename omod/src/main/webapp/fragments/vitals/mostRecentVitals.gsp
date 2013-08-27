<div id="most-recent-vitals-container">
    Loading...
</div>

<script type="text/javascript"/>
	emr.getFragmentActionWithCallback("htmlformentryui", "viewEncounterWithHtmlForm", "getAsHtml", { encounterId: ${encounterId} }, function(result) {
	    jq('#most-recent-vitals-container').html(result.html);
});
</script>
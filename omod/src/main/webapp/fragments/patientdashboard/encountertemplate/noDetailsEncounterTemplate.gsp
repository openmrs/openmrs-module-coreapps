<script type="text/template" id="noDetailsEncounterTemplate">
<li>
	<div class="encounter-date">
	    <i class="icon-time"></i>
	    <strong>
	        {{- encounter.encounterTime }}
	    </strong>
	    {{- encounter.encounterDate }}
	</div>
	<ul class="encounter-details">
	    <li> 
	        <div class="encounter-type">
	            <strong>
	                <i class="{{- config.icon }}"></i>
	                <span class="encounter-name" data-encounter-id="{{- encounter.encounterId }}">{{- encounter.encounterType.name }}</span>
	            </strong>
	        </div>
	    </li>
	    <li>
	        <div>
	            ${ ui.message("emr.by") }
	            <strong>
	                {{- encounter.encounterProviders[0] ? encounter.encounterProviders[0].provider : '' }}
	            </strong>
	            ${ ui.message("emr.in") }
	            <strong>{{- encounter.location }}</strong>
	        </div>
	    </li>
	</ul>
	{{ if ( encounter.canDelete ) { }}
	<span>
        {{ if ( config.editable ) { }}
        <% if (featureToggles.isFeatureEnabled("editAdmissionNote")) { %>
        <i class="editEncounter delete-item icon-pencil" data-patient-id="{{- patient.id }}" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("emr.edit") }"></i>
        <% } %>
        {{ } }}
	    <i class="deleteEncounterId delete-item icon-remove" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("emr.delete") }"></i>
	</span>
	{{  } }}
	<div id="encounter-summary{{- encounter.encounterId }}" class="collapse">
	    <div class="encounter-summary-container"></div>
	</div>
</li>
</script>
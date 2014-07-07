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
	            ${ ui.message("coreapps.by") }
	            <strong>
                    <span class="provider">{{- encounter.primaryProvider ? encounter.primaryProvider : '' }}</span>
	            </strong>
	            ${ ui.message("coreapps.in") }
	            <strong><span class="location">{{- encounter.location }}</span></strong>
	        </div>
	    </li>
	</ul>

	<span>

        {{ if ( config.editable && encounter.canEdit) { }}
            <i class="editEncounter delete-item icon-pencil" data-patient-id="{{- patient.id }}" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("coreapps.edit") }"></i>
        {{ } }}

        {{ if ( encounter.canDelete ) { }}
	        <i class="deleteEncounterId delete-item icon-remove" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("coreapps.delete") }"></i>
        {{  } }}
	</span>

	<div id="encounter-summary{{- encounter.encounterId }}" class="collapse">
	    <div class="encounter-summary-container"></div>
	</div>
</li>
</script>
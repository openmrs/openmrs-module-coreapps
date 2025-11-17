<%  
  ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("coreapps", "attachments/app.js")
  ui.includeJavascript("coreapps", "attachments/fragments/complexObsEncounterTemplate.js")
  
  ui.includeJavascript("uicommons", "services/obsService.js")
  ui.includeJavascript("coreapps", "attachments/directives/complexObsEncounterDirective.js")
%>

<!-- Angular widgets -->
<%
  ui.includeFragment("coreapps", "attachments/dependenciesThumbnail")
  ui.includeFragment("coreapps", "attachments/dependenciesGallery")
%>

<style>
  /* See Core App's patientDashboard.css */
  .collapse .encounter-summary-container {
    min-height: 200px; /* This is a workaround, the heigh "should" auto-adjust... */
    overflow: auto;
  }

  #encountersList .encounter-details .att_view-details {
    background: #f9f9f9;
  }
  #encountersList .encounter-details .att_view-details .hide-details {
    display: none;
  }
  #encountersList .encounter-details .att_view-details:not(.collapsed) .hide-details {
    display: inline;
  }
  #encountersList .encounter-details .att_view-details:not(.collapsed) .show-details {
    display: none;
  }
  #encountersList .encounter-details .att_view-details:not(.collapsed) .icon-caret-right {
    -moz-transform: rotate(90deg);
    -webkit-transform: rotate(90deg);
    -o-transform: rotate(90deg);
    -ms-transform: rotate(90deg);
    transform: rotate(90deg);
    -moz-transition-property: all;
    -webkit-transition-property: all;
    -o-transition-property: all;
    transition-property: all;
    -moz-transition-duration: 0.15s;
    -webkit-transition-duration: 0.15s;
    -o-transition-duration: 0.15s;
    transition-duration: 0.15s;
    -moz-transition-timing-function: ease-in-out;
    -webkit-transition-timing-function: ease-in-out;
    -o-transition-timing-function: ease-in-out;
    transition-timing-function: ease-in-out;
  }
  #encountersList .encounter-details .att_view-details i {
    font-size: 1.2em;
    padding: 0;
    position: relative;
    top: 1px;
    left: -3px;
  }
  #encountersList .encounter-details .att_view-details:hover {
    text-decoration: none;
  }
</style>

<div id="att-fragment-encounter-template" ng-controller="EncounterTemplateCtrl" ng-init="init()">

  <script type="text/template" id="complexObsEncounterTemplate">
  <li>
    <div class="encounter-date ${ui.convertTimezones() ? 'rfc3339-date' : ''}">
      <i class="icon-time"></i>
      <strong class="encounter-time">
        {{- encounter.encounterTime }}
      </strong>
      <spann class="encounter-datetime">
        {{- encounter.encounterDate }}
      </spann>
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
          <strong class="provider">
            {{- encounter.primaryProvider ? encounter.primaryProvider : '' }}
          </strong>
          ${ ui.message("coreapps.in") }
          <strong class="location">{{- encounter.location }}</strong>
        </div>
      </li>
      <li>
        <div class="details-action">
          <a class="att_view-details collapsed" href='javascript:void(0);' data-encounter-id="{{- encounter.encounterId }}" data-encounter-form="{{- encounter.form != null}}" data-display-with-html-form="{{- config.displayWithHtmlForm }}" data-target="#encounter-summary{{- encounter.encounterId }}" data-toggle="collapse"
            data-encounter-uuid="{{- encounter.uuid }}"
            data-encounter-type-uuid="{{- encounter.encounterType.uuid }}">
              <span class="show-details">${ ui.message("coreapps.patientDashBoard.showDetails")}</span>
              <span class="hide-details">${ ui.message("coreapps.patientDashBoard.hideDetails")}</span>
              <i class="icon-caret-right"></i>
          </a>
        </div>
      </li>
    </ul>

    <span>      
      {{ if ( config.editable != "false"  && config.editable && encounter.canEdit ) { }}
        <i class="editEncounter delete-item icon-pencil" data-patient-id="{{- patient.id }}" data-encounter-id="{{- encounter.encounterId }}" {{ if (config.editUrl) { }} data-edit-url="{{- config.editUrl }}" {{ } }} title="${ ui.message("coreapps.edit") }"></i>
      {{ } }}
    
      {{ if ( config.deletable != "false"  && config.deletable && encounter.canDelete ) { }}
        <i class="deleteEncounterId delete-item icon-remove" data-visit-id="{{- encounter.visitId }}" data-encounter-id="{{- encounter.encounterId }}" title="${ ui.message("coreapps.delete") }"></i>
      {{  } }}
    </span>

    <div id="encounter-summary{{- encounter.encounterId }}">
      <div class="encounter-summary-container"></div>
    </div>

  </li>
  </script>

</div>

<script type="text/javascript">
  // manually bootstrap angular app, in case there are multiple angular apps on a page
  angular.bootstrap('#att-fragment-encounter-template', ['att.fragment.encounterTemplate']);
</script>

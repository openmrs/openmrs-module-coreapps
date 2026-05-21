import angular from 'angular';

// angular.lowercase/uppercase were removed in 1.8.x but are still used by angular-translate
if (!angular.lowercase) {
    angular.lowercase = s => s.toLowerCase();
    angular.uppercase = s => s.toUpperCase();
}

import DataIntegrityViolations from './dataintegrityviolations';
import LatestObsForConceptList from './latestobsforconceptlist';
import ObsAcrossEncounters from './obsacrossencounters';
import ObsGraph from './obsgraph';
import Programs from './programs';
import ProgramStatistics from './programstatistics';
import ProgramStatus from './programstatus';
import Relationships from './relationships';
import VisitByEncounterType from './visitbyencountertype';
import BahmniAppointments from './bahmniappointments';

export default angular.module("openmrs-contrib-dashboardwidgets", [ DataIntegrityViolations, LatestObsForConceptList,
    ObsAcrossEncounters, ObsGraph, Programs, ProgramStatistics, ProgramStatus, Relationships, VisitByEncounterType ,BahmniAppointments]).name;


angular.element(document).ready(function() {
    for (var dashboardwidget of document.getElementsByClassName('openmrs-contrib-dashboardwidgets')) {
        angular.bootstrap(dashboardwidget, [ 'openmrs-contrib-dashboardwidgets' ]);
    }
});
import DataIntegrityViolations from './dataintegrityviolations';
import LatestObsForConceptList from './latestobsforconceptlist';
import ObsAcrossEncounters from './obsacrossencounters';
import ObsGraph from './obsgraph';
import Programs from './programs';
import ProgramStatus from './programstatus';
import Relationships from './relationships';
import VisitByEncounterType from './visitbyencountertype';

export default angular.module("openmrs-contrib-dashboardwidgets", [ DataIntegrityViolations, LatestObsForConceptList,
    ObsAcrossEncounters, ObsGraph, Programs, ProgramStatus, Relationships, VisitByEncounterType]).name;



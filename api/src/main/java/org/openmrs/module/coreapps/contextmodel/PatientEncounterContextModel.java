package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Encounter;

import java.util.Date;

public class PatientEncounterContextModel {

    Date encounterDatetime;
    String encounterTypeName;
    String encounterTypeUuid;

    public PatientEncounterContextModel(Encounter encounter) {
        this.encounterDatetime = encounter.getEncounterDatetime();
        this.encounterTypeName = encounter.getEncounterType().getName();
        this.encounterTypeUuid = encounter.getEncounterType().getUuid();
    }

    public Date getEncounterDatetime() {
        return encounterDatetime;
    }

    public String getEncounterTypeName() {
        return encounterTypeName;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

}

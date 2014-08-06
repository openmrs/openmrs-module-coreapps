package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Patient;

/**
 * A very simple view of a patient, suitable for use in an app contextModel.
 * Ideally we'll eventually replace this with the actual patient web service representation, so we should keep anything
 * represented here consistent with that view
 */
public class PatientContextModel {

    private PersonContextModel person;

    /**
     * @deprecated this will disappear for consistency with web services, so prefer to use the uuid property
     */
    @Deprecated
    private Integer patientId;

    private String uuid;

    public PatientContextModel(Patient patient) {
        this.person = new PersonContextModel(patient);
        this.patientId = patient.getPatientId();
        this.uuid = patient.getUuid();
    }

    public PersonContextModel getPerson() {
        return person;
    }

    /**
     * @deprecated this will disappear for consistency with web services, so prefer to use the uuid property
     */
    @Deprecated
    public Integer getPatientId() {
        return patientId;
    }

    public String getUuid() {
        return uuid;
    }

}

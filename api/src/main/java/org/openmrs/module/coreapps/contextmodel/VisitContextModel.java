package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

/**
 * A very simple view of a visit, suitable for use in an app contextModel.
 */
public class VisitContextModel {

    private int id;
    private boolean active;
    private boolean admitted;

    public VisitContextModel(VisitDomainWrapper visit) {
        this.id = visit.getVisitId();
        this.active = visit.isOpen();
        this.admitted = visit.isAdmitted();
    }

    public int getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAdmitted() {
        return admitted;
    }

}

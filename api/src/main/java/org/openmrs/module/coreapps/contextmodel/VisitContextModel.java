package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;

/**
 * A very simple view of a visit, suitable for use in an app contextModel.
 */
public class VisitContextModel {

    private int id;
    private boolean active;
    private boolean admitted;
    private Long stopDate;

    public VisitContextModel(VisitDomainWrapper visit) {
        this.id = visit.getVisitId();
        this.active = visit.isOpen();
        this.admitted = visit.isAdmitted();

        Date stopDatetime = visit.getStopDatetime();

        if(stopDatetime!=null){
            this.stopDate = stopDatetime.getTime();
        }else{
            this.stopDate = 0L;
        }
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

    public Long getStopDate(){
        return  stopDate;
    }

}

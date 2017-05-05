package org.openmrs.module.coreapps.contextmodel;

import java.util.Date;

import org.openmrs.VisitType;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;

/**
 * A very simple view of a visit, suitable for use in an app contextModel.
 */
public class VisitContextModel {

    private int id;
    private String uuid;
    private boolean active;
    private boolean admitted;
    private Long stopDatetimeInMilliseconds;
    private Long startDatetimeInMilliseconds;
    private Date stopDatetime;
    private SimpleObject visitType;

    public VisitContextModel(VisitDomainWrapper visit) {
        this.id = visit.getVisitId();
        this.uuid = visit.getVisit().getUuid();
        this.active = visit.isOpen();
        this.admitted = visit.isAdmitted();
        this.startDatetimeInMilliseconds = visit.getStartDatetime().getTime();

        Date stopDatetime = visit.getStopDatetime();
        this.stopDatetime = stopDatetime;

        if (stopDatetime != null) {
            this.stopDatetimeInMilliseconds = stopDatetime.getTime();
        } else {
            this.stopDatetimeInMilliseconds = null;
        }
        
       VisitType visitType = visit.getVisit().getVisitType();
        if (visitType != null) {
            this.visitType = new SimpleObject(visitType);
        }
    }

    public int getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public Long getStopDatetimeInMilliseconds(){
        return stopDatetimeInMilliseconds;
    }

    public Long getStartDatetimeInMilliseconds(){
        return startDatetimeInMilliseconds;
    }

    public Date getStopDatetime() {
        return stopDatetime;
    }
    
    public SimpleObject getVisitType() {
        return visitType;
    }
}

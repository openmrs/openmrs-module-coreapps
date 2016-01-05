package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;

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

    public VisitContextModel(VisitDomainWrapper visit) {
        this.id = visit.getVisitId();
        this.uuid = visit.getVisit().getUuid();
        this.active = visit.isOpen();
        this.admitted = visit.isAdmitted();
        this.startDatetimeInMilliseconds = visit.getStartDatetime().getTime();

        Date stopDatetime = visit.getStopDatetime();
        this.stopDatetime = stopDatetime;

        if(stopDatetime!=null){
            this.stopDatetimeInMilliseconds = stopDatetime.getTime();
        }else{
            this.stopDatetimeInMilliseconds = null;
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
}

package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Visit;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.VisitType;

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
    private VisitType visitType;
    private Date stopDatetime;

    public VisitContextModel(VisitDomainWrapper visit) {
        this.id = visit.getVisitId();
        this.uuid = visit.getVisit().getUuid();
        this.active = visit.isOpen();
        this.admitted = visit.isAdmitted();
        this.startDatetimeInMilliseconds = visit.getStartDatetime().getTime();
        this.visitType = visit.getVisit().getVisitType();
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
    //VisitType getter
    public VisitType getVisitType() {
        return visitType;
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

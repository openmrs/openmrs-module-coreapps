package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Encounter;
import org.openmrs.VisitType;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private List<SimpleObject> encounters;

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

        VisitType visitType = visit.getVisit().getVisitType();
        if (visitType != null) {
          this.visitType = (SimpleObject) ConversionUtil.convertToRepresentation(visitType, Representation.DEFAULT);
        }

        List<Encounter> encounters = visit.getSortedEncounters();
        this.encounters = new ArrayList<SimpleObject>();

        if (encounters != null) {
            for (Encounter encounter : encounters) {
                this.encounters.add((SimpleObject) ConversionUtil.convertToRepresentation(encounter, new CustomRepresentation("(uuid,encounterType:(name,uuid),encounterDatetime)")));
            }
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

    public List<SimpleObject> getEncounters() {
        return encounters;
    }
}

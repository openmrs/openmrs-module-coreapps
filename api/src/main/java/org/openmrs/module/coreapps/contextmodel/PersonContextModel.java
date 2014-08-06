package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Person;

import java.util.Date;

/**
 * A very simple view of a person, suitable for use in an app contextModel.
 * Ideally we'll eventually replace this with the actual patient web service representation, so we should keep anything
 * represented here consistent with that view
 */
public class PersonContextModel {

    private String uuid;

    private Date birthdate;

    private Boolean birthdateEstimated;

    private String gender;

    private Boolean dead;

    public PersonContextModel(Person person) {
        this.uuid = person.getUuid();
        this.birthdate = person.getBirthdate();
        this.birthdateEstimated = person.getBirthdateEstimated();
        this.gender = person.getGender();
        this.dead = person.getDead();
    }

    public String getUuid() {
        return uuid;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public Boolean getBirthdateEstimated() {
        return birthdateEstimated;
    }

    public String getGender() {
        return gender;
    }

    public Boolean getDead() {
        return dead;
    }

}

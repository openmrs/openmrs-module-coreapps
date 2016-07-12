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

    private boolean birthdateEstimated;

    private String gender;

    private boolean dead;

    private Integer age;

    private Date deathdate;

    public PersonContextModel(Person person) {
        this.uuid = person.getUuid();
        this.birthdate = person.getBirthdate();
        this.birthdateEstimated = person.getBirthdateEstimated();
        this.gender = person.getGender();
        this.dead = person.getDead();
        this.age = person.getAge();
        this.deathdate = person.getDeathDate();
    }

    public String getUuid() {
        return uuid;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public boolean isBirthdateEstimated() {
        return birthdateEstimated;
    }

    public String getGender() {
        return gender;
    }

    public boolean isDead() {
        return dead;
    }

    public Integer getAge() {
        return age;
    }

    public Date getDeathdate() {
        return deathdate;
    }

}

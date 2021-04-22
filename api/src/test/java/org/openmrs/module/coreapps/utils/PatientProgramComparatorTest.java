package org.openmrs.module.coreapps.utils;

import org.joda.time.DateTime;
import org.junit.Test;
import org.openmrs.PatientProgram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PatientProgramComparatorTest {

    @Test
    public void patientProgramComparator_shouldProperlySortByDateEnrolled() {

        PatientProgram p1 = new PatientProgram();
        PatientProgram p2 = new PatientProgram();
        PatientProgram p3 = new PatientProgram();

        p1.setId(1);
        p1.setDateEnrolled(new DateTime(2020,3,31,0,0).toDate());

        p2.setId(2);
        p2.setDateEnrolled(new DateTime(2020,4,4 ,0,0).toDate());

        p3.setId(3);
        p3.setDateEnrolled(new DateTime(2020,4,2,0,0).toDate());

        List<PatientProgram> list = new ArrayList<PatientProgram>();
        list.add(p1);
        list.add(p2);
        list.add(p3);

        Collections.sort(list, new PatientProgramComparator());

        assertThat(list.get(0).getId(), is(2));
        assertThat(list.get(1).getId(), is(3));
        assertThat(list.get(2).getId(), is(1));
    }

    @Test
    public void patientProgramComparator_shouldSortActiveFirstIfSameDateEnrolled() {

        PatientProgram p1 = new PatientProgram();
        PatientProgram p2 = new PatientProgram();

        p1.setId(1);
        p1.setDateEnrolled(new DateTime(2020,3,31,0,0).toDate());
        p1.setDateCompleted(new DateTime(2020, 4,4,0,0).toDate());

        p2.setId(2);
        p2.setDateEnrolled(new DateTime(2020,3,31 ,0,0).toDate());

        List<PatientProgram> list = new ArrayList<PatientProgram>();
        list.add(p1);
        list.add(p2);

        Collections.sort(list, new PatientProgramComparator());

        assertThat(list.get(0).getId(), is(2));
        assertThat(list.get(1).getId(), is(1));
    }

    @Test
    public void patientProgramComparator_shouldSortByDateCompletedFirstIfSameDateEnrolled() {

        // technically, this should never happen, but just in case

        PatientProgram p1 = new PatientProgram();
        PatientProgram p2 = new PatientProgram();

        p1.setId(1);
        p1.setDateEnrolled(new DateTime(2020,3,31,0,0).toDate());
        p1.setDateCompleted(new DateTime(2020, 4,4,0,0).toDate());

        p2.setId(2);
        p2.setDateEnrolled(new DateTime(2020,3,31 ,0,0).toDate());
        p2.setDateCompleted(new DateTime(2020, 4,6,0,0).toDate());

        List<PatientProgram> list = new ArrayList<PatientProgram>();
        list.add(p1);
        list.add(p2);

        Collections.sort(list, new PatientProgramComparator());

        assertThat(list.get(0).getId(), is(2));
        assertThat(list.get(1).getId(), is(1));
    }

    @Test
    public void patientProgramComparator_shouldSortByDateCreatedIfSameDateEnrolledAndDateCompleted() {

        // technically, this should never happen, but just in case

        PatientProgram p1 = new PatientProgram();
        PatientProgram p2 = new PatientProgram();

        p1.setId(1);
        p1.setDateEnrolled(new DateTime(2020,3,31,0,0).toDate());
        p1.setDateCompleted(new DateTime(2020, 4,4,0,0).toDate());
        p1.setDateCreated(new DateTime(2020, 4,12,0,0).toDate());

        p2.setId(2);
        p2.setDateEnrolled(new DateTime(2020,3,31 ,0,0).toDate());
        p2.setDateCompleted(new DateTime(2020, 4,4,0,0).toDate());
        p2.setDateCreated(new DateTime(2020, 4,14,0,0).toDate());

        List<PatientProgram> list = new ArrayList<PatientProgram>();
        list.add(p1);
        list.add(p2);

        Collections.sort(list, new PatientProgramComparator());

        assertThat(list.get(0).getId(), is(2));
        assertThat(list.get(1).getId(), is(1));
    }


}

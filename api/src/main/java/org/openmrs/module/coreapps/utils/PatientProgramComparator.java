package org.openmrs.module.coreapps.utils;

import org.openmrs.PatientProgram;
import org.openmrs.util.OpenmrsUtil;

import java.util.Comparator;

/**
 * Note that this sorts the *most recent* first
 */
public class PatientProgramComparator implements Comparator<PatientProgram> {

    @Override
    public int compare(PatientProgram patientProgram1, PatientProgram patientProgram2) {

        // first sort by date enrolled (which should never be null, but we will sort null earliest)
        int result = OpenmrsUtil.compareWithNullAsEarliest(patientProgram2.getDateEnrolled(), patientProgram1.getDateEnrolled());

        if (result != 0) {
            return result;
        }

        // then by date completed, with "active" (no completion date) ranking first
        // assumption: never should be two active programs (ie both date completed should not be null)
        result = OpenmrsUtil.compareWithNullAsLatest(patientProgram2.getDateCompleted(), patientProgram1.getDateCompleted());

        if (result != 0) {
            return result;
        }

        result = OpenmrsUtil.compare(patientProgram2.getDateCreated(), patientProgram1.getDateCreated());

        if (result != 0) {
            return result;
        }

        // just something to make this deterministic
        else {
            return patientProgram1.getUuid().compareTo(patientProgram2.getUuid());
        }
    }
}

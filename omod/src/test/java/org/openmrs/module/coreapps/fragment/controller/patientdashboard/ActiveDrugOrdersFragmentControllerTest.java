package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class ActiveDrugOrdersFragmentControllerTest {
	
	private ActiveDrugOrdersFragmentController controller;

	private FragmentConfiguration config;
	
	private FragmentModel model;

	private PatientDomainWrapper wrapper;
	
	private Patient patient;

	private PatientService patientService;
	
	private OrderService orderService;

	@Before
	public void setup() {
		
	}

	@Test
	public void controller_should() {
		
	}
}

package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.UiUtils;

@RunWith(MockitoJUnitRunner.class)
public class ActiveDrugOrdersFragmentControllerTest {
	
	private ActiveDrugOrdersFragmentController fragmentController;

	private FragmentConfiguration config;
	
	private FragmentModel model;

	private PatientDomainWrapper wrapper;
	
	private Patient patient;
	
	private OrderType drugOrders;
	
	private List<Order> activeDrugOrders;
	
	private AppDescriptor app;

	private BasicUiUtils ui;
	
	@Mock
	private PatientService patientService;
	
	@Mock
	private OrderService orderService;

	@Before
	public void setup() throws Exception {
		fragmentController = new ActiveDrugOrdersFragmentController();
		patient = new Patient(1);
		patient.setUuid("patient-uuid");
		drugOrders = new OrderType(1); 
		activeDrugOrders = new ArrayList<Order>();
		activeDrugOrders.add(new Order(1));
		activeDrugOrders.add(new Order(2));
		wrapper = new PatientDomainWrapper();
		wrapper.setPatient(patient);
		config = new FragmentConfiguration();
		model = new FragmentModel();
		ui = new BasicUiUtils();
		
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("dispensedMedication_app.json");    	
    	app = new ObjectMapper().readValue(inputStream, new TypeReference<AppDescriptor>() {});
    	
		
		when(patientService.getPatient(1)).thenReturn(patient);
		when(orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID)).thenReturn(drugOrders);
		when(orderService.getActiveOrders(patient, drugOrders, null, null)).thenReturn(activeDrugOrders);
	}

	@Test
	public void controller_shouldAddPatientAndActiveDrugOrdersToModelGivenPatientObject() throws Exception {
		// setup
		config.addAttribute("patient", patient);

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(patient, model.getAttribute("patient"));
		assertEquals(activeDrugOrders, model.getAttribute("activeDrugOrders"));
	}
	
	@Test
	public void controller_shouldAddPatientAndActiveDrugOrdersToModelGivenPatientWrapper() throws Exception {
		// setup
		config.addAttribute("patient", wrapper);

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(patient, model.getAttribute("patient"));
		assertEquals(activeDrugOrders, model.getAttribute("activeDrugOrders"));
	}
	
	@Test
	public void controller_shouldAddPatientAndActiveDrugOrdersToModelGivenPatientId() throws Exception {
		// setup
		config.addAttribute("patientId", 1);

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(patient, model.getAttribute("patient"));
		assertEquals(activeDrugOrders, model.getAttribute("activeDrugOrders"));
	}
	
	@Test
	public void controller_shouldAddAppConfigToModelGivenConfiguredApp() throws Exception {
		// setup
		config.addAttribute("patientId", 1);

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(patient, model.getAttribute("patient"));
		assertEquals(activeDrugOrders, model.getAttribute("activeDrugOrders"));
		assertEquals(app.getConfig().get("detailsUrl").asText(), model.getAttribute("detailsUrl").toString());
		assertEquals(app.getConfig().get("displayActivationDate").asBoolean(), (Boolean) model.getAttribute("displayActivationDate"));
	}
	
	@Test
	public void controller_shouldDefaultDetailsUrlToNullIfMissingInConfiguredApp() throws Exception {
		// setup
		config.addAttribute("patientId", 1);
		app.getConfig().remove("detailsUrl");

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(null, app.getConfig().get("detailsUrl"));
		
	}

	@Test
	public void controller_shouldAddDefaultReturnUrlToModelGivenConfigParameterMissing() throws Exception {
		// setup
		config.addAttribute("patientId", 1);
		app.getConfig().remove("returnUrl");

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals("/openmrs/coreapps/clinicianfacing/patient.page?patientId=patient-uuid&", model.getAttribute("returnUrl"));
	}

	@Test
	public void controller_shouldAddReturnUrlToModelGivenConfigParameterPresent() throws Exception {
		// setup
		config.addAttribute("patientId", 1);

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals("/openmrs/custom/returnUrl.page?patientId={{patientUuid}}", model.getAttribute("returnUrl"));
	}
	
	@Test
	public void controller_shouldDefaultDisplayActivationDateToFalseGivenItIsMissingInConfiguredApp() throws Exception {
		// setup
		config.addAttribute("patientId", 1);
		app.getConfig().remove("displayActivationDate");

		// replay
		fragmentController.controller(config, app, patientService, orderService, model, (UiUtils) ui);
		
		// verify
		assertEquals(Boolean.FALSE, (Boolean) model.getAttribute("displayActivationDate"));
	}

	private class BasicUiUtils extends UiUtils {

		public BasicUiUtils() {
			super();
		}

		@Override
		public String pageLink(String providerName, String pageName, Map<String, Object> params) {
			return "/openmrs" + pageLinkWithoutContextPath(providerName, pageName, params);
		}
	}
}

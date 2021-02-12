package org.openmrs.module.coreapps.fragment.controller.patientsearch;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsConstants;

@RunWith(MockitoJUnitRunner.class)
public class PatientSearchWidgetFragmentControllerTest {
	
	private PatientSearchWidgetFragmentController fragmentController;
	
	private FragmentModel model;

	private UiSessionContext sessionContext;

	private HttpServletRequest request;

	@Mock
	private AdministrationService administrationService;

	@Mock
	private AppFrameworkService appFrameworkService;

	@Mock
	private PatientService patientService;

	@Mock
	private MessageSourceService messageSourceService;

	@Before
	public void setup() throws Exception {
		fragmentController = new PatientSearchWidgetFragmentController();
		model = new FragmentModel();
		sessionContext = mock(UiSessionContext.class);
		request = mock(HttpServletRequest.class);
		when(administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE, "en")).thenReturn("en");
		when(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATE_FORMAT)).thenReturn("yyyy-MM-dd");
		PatientIdentifierType type1 = new PatientIdentifierType();
		type1.setUuid("type-1-uuid");
		type1.setName("type-1-name");
		when(patientService.getPatientIdentifierTypeByUuid("type-1-uuid")).thenReturn(type1);
		when(patientService.getPatientIdentifierTypeByName("type-1-name")).thenReturn(type1);
		PatientIdentifierType type2 = new PatientIdentifierType();
		type2.setUuid("type-2-uuid");
		type2.setName("type-2-name");
		when(patientService.getPatientIdentifierTypeByUuid("type-2-uuid")).thenReturn(type2);
		when(patientService.getPatientIdentifierTypeByName("type-2-name")).thenReturn(type2);

		when(messageSourceService.getMessage(anyString())).thenAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocationOnMock) {
				String code = invocationOnMock.getArguments()[0].toString();
				if (code.equals("messagecode.label")) {
					return "My Message Code Label";
				}
				return code;
			}
		});
	}

	@Test
	public void controller_shouldReturnEmptyJsonArrayIfNoExtraIdentifierTypesConfigured() {
		ObjectNode appConfig = objectNode();
		runController(appConfig);
		assertEquals("[]", model.getAttribute("identifierTypes"));
	}

	@Test
	public void controller_shouldReturnExtraIdentifierTypeConfiguredByName() {
		ObjectNode appConfig = objectNode("identifierTypes", arrayNode(
				objectNode("name", "type-1-name")
		));
		runController(appConfig);
		assertEquals("[{\"label\":\"type-1-name\",\"uuid\":\"type-1-uuid\"}]", model.getAttribute("identifierTypes"));
	}

	@Test
	public void controller_shouldReturnExtraIdentifierTypeConfiguredByUuid() {
		ObjectNode appConfig = objectNode("identifierTypes", arrayNode(
				objectNode("uuid", "type-1-uuid")
		));
		runController(appConfig);
		assertEquals("[{\"label\":\"type-1-name\",\"uuid\":\"type-1-uuid\"}]", model.getAttribute("identifierTypes"));
	}

	@Test
	public void controller_shouldReturnIdentifierNameIfNoLabelConfigured() {
		ObjectNode appConfig = objectNode("identifierTypes", arrayNode(
				objectNode("uuid", "type-1-uuid")
		));
		runController(appConfig);
		assertEquals("[{\"label\":\"type-1-name\",\"uuid\":\"type-1-uuid\"}]", model.getAttribute("identifierTypes"));
	}

	@Test
	public void controller_shouldReturnLabelIfConfigured() {
		ObjectNode appConfig = objectNode("identifierTypes", arrayNode(
				objectNode("uuid", "type-1-uuid", "label", "Type 1 Label")
		));
		runController(appConfig);
		assertEquals("[{\"label\":\"Type 1 Label\",\"uuid\":\"type-1-uuid\"}]", model.getAttribute("identifierTypes"));
	}

	@Test
	public void controller_shouldReturnLabelFromMessageCodeIfConfigured() {
		ObjectNode appConfig = objectNode("identifierTypes", arrayNode(
				objectNode("uuid", "type-1-uuid", "label", "messagecode.label")
		));
		runController(appConfig);
		assertEquals("[{\"label\":\"My Message Code Label\",\"uuid\":\"type-1-uuid\"}]", model.getAttribute("identifierTypes"));
	}

	protected void runController(ObjectNode appConfig, Boolean showLastViewPatients, String searchByParam, String regLink) {
		fragmentController.controller(
				model, sessionContext, request,
				administrationService, appFrameworkService, patientService, messageSourceService,
				appConfig, showLastViewPatients, searchByParam, regLink
		);
	}

	protected void runController(ObjectNode appConfig) {
		runController(appConfig, false, "", "/");
	}

	protected ArrayNode arrayNode(ObjectNode ... nodes) {
		ArrayNode arrayNode = new ObjectMapper().createArrayNode();
		for (int i = 0; i < nodes.length; i++) {
			arrayNode.add(nodes[i]);
		}
		return arrayNode;
	}

	protected ObjectNode objectNode(Object ... obj) {

		ObjectNode objectNode = new ObjectMapper().createObjectNode();

		for (int i = 0; i < obj.length; i=i+2) {
			String key = (String) obj[i];
			Object value = obj[i+1];

			if (value instanceof Boolean) {
				objectNode.put(key, (Boolean) value);
			}
			else if (value instanceof String) {
				objectNode.put(key, (String) value);
			}
			else if (value instanceof Integer) {
				objectNode.put(key, (Integer) value);
			}
			else if (value instanceof ArrayNode) {
				objectNode.put(key, (ArrayNode) value);
			}
			else if (value instanceof ObjectNode) {
				objectNode.put(key, (ObjectNode) value);
			}
		}

		return objectNode;
	}
}

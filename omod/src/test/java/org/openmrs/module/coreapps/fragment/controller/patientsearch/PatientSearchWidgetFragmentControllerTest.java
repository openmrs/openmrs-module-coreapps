package org.openmrs.module.coreapps.fragment.controller.patientsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

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
import org.openmrs.ui.framework.Model;
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
	public void controller_shouldReturnNullIfNoColumnsConfigured() {
		runController(null);
		assertNull(model.getAttribute("columnConfig"));
	}

	@Test
	public void controller_shouldReturnColumnDefinitionsIfConfigured() throws Exception {
		ArrayNode columnConfig = arrayNode(
			objectNode("type", "identifier"),
			objectNode("type", "name", "width", "200px"),
			objectNode("type", "gender", "label", "Sex"),
			objectNode("type", "age"),
			objectNode("type", "birthdate")
		);
		runController(columnConfig);
		assertColumnConfigContains(model, 0, "type", "identifier");
		assertColumnConfigContains(model, 1, "type", "name");
		assertColumnConfigContains(model, 1, "width", "200px");
		assertColumnConfigContains(model, 2, "type", "gender");
		assertColumnConfigContains(model, 2, "label", "Sex");
		assertColumnConfigContains(model, 3, "type", "age");
		assertColumnConfigContains(model, 4, "type", "birthdate");
	}

	@Test
	public void controller_shouldTranslateLabelsIfValidMessageCoded() throws Exception {
		ArrayNode columnConfig = arrayNode(
			objectNode("type", "identifier", "label", "EMR ID"),
			objectNode("type", "identifier", "value", "xyz", "label", "messagecode.label")
		);
		runController(columnConfig);
		assertColumnConfigContains(model, 0, "type", "identifier");
		assertColumnConfigContains(model, 0, "label", "EMR ID");
		assertColumnConfigContains(model, 1, "type", "identifier");
		assertColumnConfigContains(model, 1, "label", "My Message Code Label");
	}

	protected void assertColumnConfigContains(Model model, int index, String key, String value) throws Exception {
		String columnConfig = (String) model.getAttribute("columnConfig");
		List<Map<String, String>> config = new ObjectMapper().readValue(columnConfig, List.class);
		Map<String, String> column = config.get(index);
		String val = column.get(key);
		if (value == null) {
			assertNull(val);
		}
		else {
			assertEquals(value, val);
		}
	}

	protected void runController(ArrayNode columnConfig, Boolean showLastViewPatients, String searchByParam, String regLink) {
		fragmentController.controller(
				model, sessionContext, request, administrationService, appFrameworkService, messageSourceService,
				columnConfig, showLastViewPatients, searchByParam, regLink
		);
	}

	protected void runController(ArrayNode columnConfig) {
		runController(columnConfig, false, "", "/");
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

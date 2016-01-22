package org.openmrs.module.coreapps.htmlformentry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.test.BaseContextMockTest;
import org.openmrs.ui.framework.BasicUiUtils;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncounterDiagnosesElementTest extends BaseContextMockTest {

    @Mock
    MessageSourceService messageSourceService;

    @Before
    public void setUp() throws Exception {
        when(messageSourceService.getMessage(anyString())).thenReturn("Translated");
    }

    @Test
    public void testMessageWithoutUiUtils() throws Exception {
        assertThat(new EncounterDiagnosesElement().message("code"), is("Translated"));
    }

    @Test
    public void testMessageWithUiUtils() throws Exception {
        EncounterDiagnosesElement element = new EncounterDiagnosesElement();
        element.setUiUtils(new BasicUiUtils() {
            @Override
            public String message(String code, Object... args) {
                return "Translated";
            }
        });
        assertThat(element.message("code"), is("Translated"));
    }

    @Test
    public void testGenerateHtml() throws Exception {
        EmrApiProperties emrApiProperties = mock(EmrApiProperties.class);
        FormEntryContext context = mock(FormEntryContext.class);
        when(context.getMode()).thenReturn(FormEntryContext.Mode.VIEW);
        EncounterDiagnosesElement element = new EncounterDiagnosesElement() {
            @Override
            List<Diagnosis> getExistingDiagnoses(FormEntryContext context, DiagnosisMetadata diagnosisMetadata) {
                return Arrays.asList(new Diagnosis(new CodedOrFreeTextAnswer("Some disease")));
            }
        };
        element.setEmrApiProperties(emrApiProperties);

        String html = element.generateHtml(context);
        assertTrue(html.indexOf("Translated") >= 0);
    }
}
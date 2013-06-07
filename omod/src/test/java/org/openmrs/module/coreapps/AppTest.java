package org.openmrs.module.coreapps;

import org.junit.Test;
import org.openmrs.module.appframework.AppTestUtil;
import org.openmrs.module.appframework.domain.AppDescriptor;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class AppTest {

    @Test
    public void testFindPatientAppIsLoaded() throws Exception {
        AppDescriptor app = AppTestUtil.getAppDescriptor("coreapps.findPatient");
        assertThat(app.getOrder(), is(2));
    }

    @Test
    public void testFindPatientAppHasHomepageExtension() throws Exception {
        AppDescriptor app = AppTestUtil.getAppDescriptor("coreapps.findPatient");
        assertThat(app.getExtensions(), hasSize(1));
        assertThat(app.getExtensions().get(0).getExtensionPointId(), is("org.openmrs.referenceapplication.homepageLink"));
    }

}

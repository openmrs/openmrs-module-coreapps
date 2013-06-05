package org.openmrs.module.coreapps;

import org.junit.Test;
import org.openmrs.module.appframework.AppTestUtil;
import org.openmrs.module.appframework.domain.AppDescriptor;

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

}

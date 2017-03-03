package org.openmrs.module.coreapps.fragment.controller.dashboardwidgets;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

import java.io.IOException;
import java.util.Map;

public class DashboardWidgetFragmentController {

    public void controller(FragmentConfiguration config,@FragmentParam("app") AppDescriptor app) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> appConfig = mapper.convertValue(app.getConfig(), Map.class);

        config.merge(appConfig);
    }
}

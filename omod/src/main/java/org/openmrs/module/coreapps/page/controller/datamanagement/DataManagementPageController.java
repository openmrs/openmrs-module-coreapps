package org.openmrs.module.coreapps.page.controller.datamanagement;


import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Collections;
import java.util.List;

public class DataManagementPageController {

    public static final String DATA_MANAGEMENT_EXTENSION_POINT = "dataManagement.apps";

    public void controller(PageModel model, UiSessionContext emrContext,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) {

        emrContext.requireAuthentication();

        List<Extension> extensions = appFrameworkService.getExtensionsForCurrentUser(DATA_MANAGEMENT_EXTENSION_POINT);

        Collections.sort(extensions);
        model.addAttribute("extensions", extensions);
    }

}

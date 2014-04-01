package org.openmrs.module.coreapps.page.controller.systemadministration;

import java.util.Collections;
import java.util.List;

import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

public class SystemAdministrationPageController {

    public static final String SYSTEM_ADMINISTRATION_EXTENSION_POINT = "systemAdministration.apps";

    public void controller(PageModel model, UiSessionContext emrContext,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) {

        emrContext.requireAuthentication();

        List<Extension> extensions = appFrameworkService.getExtensionsForCurrentUser(SYSTEM_ADMINISTRATION_EXTENSION_POINT);

        Collections.sort(extensions);
        model.addAttribute("extensions", extensions);
    }

}

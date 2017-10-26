package org.openmrs.module.coreapps.page.controller.applist;

import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

public class AppListPageController {

    public void controller(@RequestParam("app") AppDescriptor app, PageModel model, UiSessionContext emrContext,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) {

        emrContext.requireAuthentication();

        List<Extension> extensions = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".apps");

        Collections.sort(extensions);

        model.addAttribute("app", app);
        model.addAttribute("extensions", extensions);

    }
}

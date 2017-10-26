package org.openmrs.module.coreapps.page.controller.summarydashboard;

import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

public class SummaryDashboardPageController {

    public Object controller(@RequestParam("app") AppDescriptor app,
                             @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                             PageModel model,
                             UiSessionContext sessionContext) {

        if (!Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_SUMMARY_DASHBOARD)) {
            return new Redirect("coreapps", "noAccess", "");
        }

        model.addAttribute("app", app);

        AppContextModel contextModel = sessionContext.generateAppContextModel();
        model.addAttribute("appContextModel", contextModel);

        List<Extension> actions = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".actions", contextModel);
        Collections.sort(actions);
        model.addAttribute("actions", actions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".includeFragments", contextModel);
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);

        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".firstColumnFragments", contextModel);
        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);

        List<Extension> secondColumnFragments = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".secondColumnFragments", contextModel);
        Collections.sort(secondColumnFragments);
        model.addAttribute("secondColumnFragments", secondColumnFragments);

        return null;
    }
}

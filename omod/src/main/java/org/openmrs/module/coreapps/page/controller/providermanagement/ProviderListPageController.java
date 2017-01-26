package org.openmrs.module.coreapps.page.controller.providermanagement;

import org.openmrs.api.context.Context;
import org.openmrs.module.providermanagement.Provider;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.List;

public class ProviderListPageController {

    public void get(PageModel model,
                    @SpringBean("providerManagementService") ProviderManagementService providerManagementService
                    ) {

        List<ProviderRole> providerRoleList = providerManagementService.getAllProviderRoles(true);
        List<Provider> providersByRoles = Context.getService(ProviderManagementService.class).getProvidersByRoles(providerRoleList);
        model.addAttribute("providersList", providersByRoles);
    }
}

package org.openmrs.module.coreapps.page.controller.providermanagement;

import org.openmrs.api.context.Context;
import org.openmrs.module.providermanagement.Provider;
import org.openmrs.module.providermanagement.ProviderManagementUtils;
import org.openmrs.module.providermanagement.relationship.ProviderPersonRelationship;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.module.providermanagement.exception.PersonIsNotProviderException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderListPageController {

    public void get(PageModel model,
                    @SpringBean("providerManagementService") ProviderManagementService providerManagementService
                    ) throws PersonIsNotProviderException {

        Map<Provider, List<ProviderPersonRelationship>> providers = new HashMap<Provider, List<ProviderPersonRelationship>>();
        List<ProviderRole> providerRoleList = providerManagementService.getRestrictedProviderRoles(false);
        if (providerRoleList != null && providerRoleList.size() > 0 ) {
            List<Provider> providersByRoles = Context.getService(ProviderManagementService.class).getProvidersByRoles(providerRoleList);
            for (Provider providerByRole : providersByRoles) {
                List<ProviderPersonRelationship> supervisorsForProvider = ProviderManagementUtils.getSupervisors(providerByRole);
                if (supervisorsForProvider == null) {
                    supervisorsForProvider = new ArrayList<ProviderPersonRelationship>();
                }
                providers.put(providerByRole, supervisorsForProvider);
            }
        }
        model.addAttribute("providersList", providers);
    }
}

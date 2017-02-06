package org.openmrs.module.coreapps.page.controller.providermanagement;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.providermanagement.Provider;
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

        Map<Provider, List<Person>> providers = new HashMap<Provider, List<Person>>();
        List<ProviderRole> providerRoleList = providerManagementService.getAllProviderRoles(true);
        List<Provider> providersByRoles = Context.getService(ProviderManagementService.class).getProvidersByRoles(providerRoleList);
        for (Provider providerByRole : providersByRoles) {
            List<Person> supervisorPersons = providerManagementService.getSupervisorsForProvider(providerByRole.getPerson());
            if (supervisorPersons == null) {
                supervisorPersons = new ArrayList<Person>();
            }
            providers.put(providerByRole, supervisorPersons);
        }
        model.addAttribute("providersList", providers);
    }
}

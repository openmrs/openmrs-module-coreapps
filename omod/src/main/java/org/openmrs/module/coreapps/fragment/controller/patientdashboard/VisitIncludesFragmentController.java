package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitIncludesFragmentController {

    @Autowired
    private CoreAppsProperties coreAppsProperties;

    public void controller(FragmentModel model) {
        model.addAttribute("visitPageUrl", coreAppsProperties.getVisitPageUrl());
    }

}

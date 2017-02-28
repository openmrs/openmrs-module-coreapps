package org.openmrs.module.coreapps.fragment.controller;

import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

abstract public class BaseFragmentController {

    public void controller(FragmentConfiguration config, AppDescriptor app) {
        config.addAttribute("icon", app.getConfig().get("icon").toString());
        config.addAttribute("label", app.getConfig().get("label").toString());
    }
}

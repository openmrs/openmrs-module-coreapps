/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.PersonAddress;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.AddressSupportCompatibility;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Uses the default address layout from core
 */
public class FormatAddressFragmentController {

    public void controller(FragmentConfiguration config,
                           FragmentModel model) {
        config.require("address");
        PersonAddress address = (PersonAddress) config.getAttribute("address");

        AddressSupportCompatibility addressSupport = Context.getRegisteredComponent("coreapps.AddressSupportCompatibility", AddressSupportCompatibility.class);
        Set<String> tokens = addressSupport.getNameMappings().keySet();

        List<String> formattedLines = new ArrayList<String>();
        for (String lineFormat : addressSupport.getLineByLineFormat()) {
            formattedLines.add(replaceWithProperties(lineFormat, tokens, address));
        }

        model.addAttribute("lines", formattedLines);
    }

    private String replaceWithProperties(String lineFormat, Set<String> tokens, Object object) {
        // replace all tokens with {{{token}}} so we can do simple string replacement below. (in case the value contains a token)
        for (String token : tokens) {
            lineFormat = lineFormat.replace(token, "{{{" + token + "}}}");
        }

        // now substitute in values for {{{token}}}
        for (String token : tokens) {
            String replacement;
            try {
                replacement = (String) PropertyUtils.getProperty(object, token);
            } catch (Exception e) {
                throw new IllegalStateException("Then token '" + token + "' in the default address layout is not an address property");
            }
            lineFormat = lineFormat.replace("{{{" + token + "}}}", replacement == null ? "" : replacement);
        }

        return lineFormat;
    }
}

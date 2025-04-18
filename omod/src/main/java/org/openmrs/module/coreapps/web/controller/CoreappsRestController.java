/**
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
package org.openmrs.module.coreapps.web.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.contextmodel.AppContextModelGenerator;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + CoreappsRestController.COREAPPS_NAMESPACE)
public class CoreappsRestController extends MainResourceController {

    public static final String COREAPPS_NAMESPACE = "/coreapps";

    @Autowired
    LocationService locationService;

    @Autowired
    ProviderService providerService;

    @Autowired
    PatientService patientService;

    @Autowired
    AppFrameworkService appFrameworkService;

    @Autowired
    AppContextModelGenerator appContextModelGenerator;

    @Autowired
    MessageSourceService messageSourceService;

    /**
     * @see org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController#getNamespace()
     */
    @Override
    public String getNamespace() {
        return RestConstants.VERSION_1 + COREAPPS_NAMESPACE;
    }

    /**
     * Retrieve all extensions for the "extensionPoint" and "patient" request parameters
     */
    @RequestMapping(value = {"/extensions"}, method = {RequestMethod.GET})
    @ResponseBody
    public Object getExtensions(HttpServletRequest request, HttpServletResponse response) throws ResponseException {
        RequestContext requestContext = RestUtil.getRequestContext(request, response);
        Representation rep = requestContext.getRepresentation();

        SimpleObject ret = new SimpleObject();
        List<Object> extensionReps = new ArrayList<>();
        ret.put("extensions", extensionReps);

        Patient patient = patientService.getPatientByUuid(requestContext.getParameter("patient"));
        String extensionPoint = request.getParameter("extensionPoint");

        if (patient != null && StringUtils.isNotBlank(extensionPoint)) {
            UiSessionContext uiSessionContext = new UiSessionContext(locationService, providerService, request);
            AppContextModel contextModel = appContextModelGenerator.generateAppContextModel(uiSessionContext, patient);
            List<Extension> extensions = appFrameworkService.getExtensionsForCurrentUser(extensionPoint, contextModel);
            Collections.sort(extensions);
            for (Extension extension : extensions) {
                if (StringUtils.isNotBlank(extension.getLabel())) {
                    extension.setLabel(messageSourceService.getMessage(extension.getLabel()));
                }
                Object extensionRep = ConversionUtil.convertToRepresentation(extension, rep);
                extensionReps.add(extensionRep);
            }
        }

        return ret;
    }
}
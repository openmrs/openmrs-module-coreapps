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

package org.openmrs.module.coreapps.page.controller.datamanagement;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.template.TemplateFactory;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.helper.BreadcrumbHelper;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class MergePatientsPageController {

    public String get(@RequestParam(required = false, value = "patient1") Patient patient1,
                    @RequestParam(required = false, value = "patient2") Patient patient2,
                    @RequestParam(value = "isUnknownPatient", defaultValue = "false") boolean isUnknownPatient,
					@RequestParam("app") AppDescriptor app,
                    @InjectBeans PatientDomainWrapper wrapper1,
                    @InjectBeans PatientDomainWrapper wrapper2,
                    HttpServletRequest request,
                    PageModel pageModel,
					UiSessionContext sessionContext,
					UiUtils ui) {

        pageModel.addAttribute("patient1", null);
        pageModel.addAttribute("patient2", null);
        pageModel.addAttribute("isUnknownPatient", isUnknownPatient);

        BreadcrumbHelper.addBreadcrumbsIfDefinedInApp(app, pageModel, ui);

        if (patient1 != null && patient2 == null ) {
            wrapper1.setPatient(patient1);
            pageModel.addAttribute("patient1", wrapper1);
            return "datamanagement/mergePatients-chooseRecords";
        }

        if (patient1 == null && patient2 == null) {
            return "datamanagement/mergePatients-chooseRecords";
        }

        if (patient1.equals(patient2)) {
			//request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "emr.mergePatients.error.samePatient");
            return "datamanagement/mergePatients-chooseRecords";
        }

        wrapper1.setPatient(patient1);
        wrapper2.setPatient(patient2);

        pageModel.addAttribute("patient1", wrapper1);
        pageModel.addAttribute("patient2", wrapper2);
        pageModel.addAttribute("overlappingVisits", wrapper1.hasOverlappingVisitsWith(patient2));

        return "datamanagement/mergePatients-confirmSamePerson";

    }

    public String post(UiUtils ui,
                       HttpServletRequest request,
                       PageModel pageModel,
                       @RequestParam("patient1") Patient patient1,
                       @RequestParam("patient2") Patient patient2,
                       @RequestParam("preferred") Patient preferred,
                       @RequestParam(value = "isUnknownPatient", defaultValue = "false") boolean isUnknownPatient,
                       @InjectBeans PatientDomainWrapper preferredWrapper,
                       @InjectBeans PatientDomainWrapper notPreferredWrapper,
                       @RequestParam("app") AppDescriptor app,
                       @SpringBean("appframeworkTemplateFactory") TemplateFactory templateFactory,
                       UiSessionContext sessionContext,
                       @SpringBean("adtService") AdtService adtService) {
        Patient notPreferred = patient1.equals(preferred) ? patient2 : patient1;

        preferredWrapper.setPatient(preferred);
        notPreferredWrapper.setPatient(notPreferred);

        if (preferredWrapper.isUnknownPatient() && !notPreferredWrapper.isUnknownPatient()){
			request.getSession().setAttribute("emr.errorMessage", ui.message("coreapps.mergePatients.unknownPatient.error"));

            preferredWrapper.setPatient(patient1);
            notPreferredWrapper.setPatient(patient2);


            Map<String, Object> params = new HashMap<String, Object>();

			params.put("app", "coreapps.mergePatients");
            params.put("patient1", preferred);
            params.put("patient2", notPreferred);
            params.put("isUnknownPatient", isUnknownPatient);

            return "redirect:" + ui.pageLink("coreapps","datamanagement/mergePatients", params);
        }

        adtService.mergePatients(preferred, notPreferred);

        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, "coreapps.mergePatients.success");
        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
        String provider = "coreapps";
        String fragment = "patientdashboard/patientDashboard";
        String returnUrl = null;
        if (app.getConfig() != null && app.getConfig().get("dashboardUrl") != null ) {
            returnUrl = app.getConfig().get("dashboardUrl").getTextValue();
        }

        if (StringUtils.isNotBlank(returnUrl)) {
            AppContextModel contextModel = sessionContext.generateAppContextModel();
            contextModel.put("patientId", preferred.getId());
            returnUrl = templateFactory.handlebars(returnUrl, contextModel);
        } else {
            returnUrl = ui.pageLink(provider, fragment, SimpleObject.create("patientId", preferred.getId()));
        }
        return "redirect:" + returnUrl;
    }

}

package org.openmrs.module.coreapps.page.controller.attachments;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.coreapps.fragment.controller.attachments.ClientConfigFragmentController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamilyMap;

@Controller
@OpenmrsProfile(modules = { "attachments:*" })
public class AttachmentsPageController {

	public void controller(@RequestParam("patient") Patient patient,
			@RequestParam(value = "visit", required = false) Visit visit, UiSessionContext sessionContext, UiUtils ui,
			@InjectBeans AttachmentsContext context, @SpringBean DomainWrapperFactory domainWrapperFactory,
			PageModel model) {
		//
		// The client-side config specific to the page
		//
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient", convertToRef(patient));

		VisitDomainWrapper visitWrapper = getVisitDomainWrapper(domainWrapperFactory, patient, visit,
				Context.getService(AdtService.class), sessionContext.getSessionLocation());
		jsonConfig.put("visit", visitWrapper == null ? null : convertVisit(visitWrapper.getVisit()));

		jsonConfig.put("contentFamilyMap", getContentFamilyMap());
		jsonConfig.put("associateWithVisit", context.associateWithVisit());

		model.put("jsonConfig", ui.toJson(jsonConfig));

		// For Core Apps's patient header.
		model.put("patient", patient);
	}

	protected VisitDomainWrapper getVisitDomainWrapper(DomainWrapperFactory domainWrapperFactory, Patient patient,
			Visit visit, AdtService adtService, Location sessionLocation) {
		VisitDomainWrapper visitWrapper = null;
		if (visit == null) {
			// Fetching the active visit, if any.
			Location visitLocation = adtService.getLocationThatSupportsVisits(sessionLocation);
			visitWrapper = adtService.getActiveVisit(patient, visitLocation);
		} else {
			visitWrapper = domainWrapperFactory.newVisitDomainWrapper(visit);
		}
		return visitWrapper;
	}

	protected Object convertVisit(Object object) {
		return object == null
				? null
				: ConversionUtil.convertToRepresentation(object,
						new CustomRepresentation(AttachmentsConstants.REPRESENTATION_VISIT));
	}

	protected Object convertToRef(Object object) {
		return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.REF);
	}
}

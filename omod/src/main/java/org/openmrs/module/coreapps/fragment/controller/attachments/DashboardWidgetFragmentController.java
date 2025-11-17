package org.openmrs.module.coreapps.fragment.controller.attachments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.Map;

public class DashboardWidgetFragmentController {

	protected final Log log = LogFactory.getLog(getClass());

	public void controller(FragmentModel model, @FragmentParam("patient") PatientDomainWrapper patientWrapper,
			UiUtils ui, @InjectBeans AttachmentsContext context) {
		Map<String, Object> jsonConfig = ClientConfigFragmentController.getClientConfig(context, ui);
		jsonConfig.put("patient",
				ConversionUtil.convertToRepresentation(patientWrapper.getPatient(), Representation.REF));
		jsonConfig.put("thumbnailCount", context.getDashboardThumbnailCount());
		model.addAttribute("jsonConfig", ui.toJson(jsonConfig));
	}
}

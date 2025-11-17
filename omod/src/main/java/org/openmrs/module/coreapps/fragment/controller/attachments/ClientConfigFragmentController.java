package org.openmrs.module.coreapps.fragment.controller.attachments;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.obs.ImageAttachmentHandler;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;

import java.util.LinkedHashMap;
import java.util.Map;

public class ClientConfigFragmentController {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Static client config builder to be available for other controllers.
	 */
	public static Map<String, Object> getClientConfig(AttachmentsContext context, UiUtils ui) {
		Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();

		String defaultDateFormat = "dd.MMM.yyyy";
		String dateFormat = Context.getAdministrationService()
				.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATE_FORMAT, defaultDateFormat);

		jsonConfig.put("locale", Context.getLocale().getLanguage());

		jsonConfig.put("uploadUrl", "/" + ui.contextPath() + "/ws" + AttachmentsConstants.UPLOAD_ATTACHMENT_URL);
		jsonConfig.put("downloadUrl", "/" + ui.contextPath() + "/ws" + AttachmentsConstants.DOWNLOAD_ATTACHMENT_URL);
		jsonConfig.put("originalView", AttachmentsConstants.ATT_VIEW_ORIGINAL);
		jsonConfig.put("thumbView", AttachmentsConstants.ATT_VIEW_THUMBNAIL);
		jsonConfig.put("dateFormat", dateFormat);

		jsonConfig.put("conceptComplexUuidList", context.getConceptComplexList());

		jsonConfig.put("thumbSize", ImageAttachmentHandler.THUMBNAIL_MAX_HEIGHT);
		jsonConfig.put("maxFileSize", context.getMaxStorageFileSize());
		jsonConfig.put("maxCompression", context.getMaxCompressionRatio());
		jsonConfig.put("allowNoCaption", context.doAllowEmptyCaption());
		jsonConfig.put("allowWebcam", context.isWebcamAllowed());
		jsonConfig.put("maxRestResults", context.getMaxRestResultsCount());

		jsonConfig.put("obsRep", "custom:" + AttachmentsConstants.REPRESENTATION_OBS);

		return jsonConfig;
	}

	/**
	 * The controller method to be invoked from the client-side. For instance with
	 * JavaScript 'emr.getFragmentActionWithCallback(...)'
	 * 
	 * @return The JSON config
	 */
	public Object get(@InjectBeans AttachmentsContext context, UiUtils ui) {
		return getClientConfig(context, ui);
	}
}

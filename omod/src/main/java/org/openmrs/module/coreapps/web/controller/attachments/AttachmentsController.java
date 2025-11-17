package org.openmrs.module.coreapps.web.controller.attachments;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.attachments.AttachmentsConstants;
import org.openmrs.module.attachments.AttachmentsContext;
import org.openmrs.module.attachments.ComplexObsSaver;
import org.openmrs.module.attachments.VisitCompatibility;
import org.openmrs.module.attachments.obs.AttachmentComplexData;
import org.openmrs.module.attachments.obs.ComplexViewHelper;
import org.openmrs.module.attachments.obs.ValueComplex;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.obs.ComplexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;

import static org.openmrs.module.attachments.AttachmentsContext.getContentFamily;

/*
 * @Deprecated and replaced by
 * {@link org.openmrs.module.attachments.rest.AttachmentBytesResource1_10 AttachmentBytesResource}
 * and
 * {@link org.openmrs.module.attachments.rest.AttachmentResource1_10 AttachmentResource}
 */
@Controller
public class AttachmentsController {

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_ATT_CONTEXT)
	protected AttachmentsContext context;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_VISIT_COMPATIBILITY)
	protected VisitCompatibility visitCompatibility;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXOBS_SAVER)
	protected ComplexObsSaver obsSaver;

	@Autowired
	@Qualifier(AttachmentsConstants.COMPONENT_COMPLEXVIEW_HELPER)
	protected ComplexViewHelper complexViewHelper;

	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = AttachmentsConstants.LEGACY_UPLOAD_ATTACHMENT_URL, method = RequestMethod.POST)
	@ResponseBody
	public Object uploadDocuments(@RequestParam("patient") Patient patient,
			@RequestParam(value = "visit", required = false) Visit visit,
			@RequestParam(value = "provider", required = false) Provider provider,
			@RequestParam("fileCaption") String fileCaption,
			@RequestParam(value = "instructions", required = false) String instructions,
			MultipartHttpServletRequest request) {
		try {
			Iterator<String> fileNameIterator = request.getFileNames(); // Looping through the uploaded file names.

			while (fileNameIterator.hasNext()) {
				String uploadedFileName = fileNameIterator.next();
				MultipartFile multipartFile = request.getFile(uploadedFileName);

				Encounter encounter = null;
				if (context.associateWithVisit()) {
					encounter = context.getAttachmentEncounter(patient, visit, provider);
				}

				if (StringUtils.isEmpty(instructions))
					instructions = ValueComplex.INSTRUCTIONS_DEFAULT;

				switch (getContentFamily(multipartFile.getContentType())) {
					case IMAGE :
						obsSaver.saveImageAttachment(visit, patient, encounter, fileCaption, multipartFile,
								instructions, null, null);
						break;

					case OTHER :
					default :
						obsSaver.saveOtherAttachment(visit, patient, encounter, fileCaption, multipartFile,
								instructions, null ,null);
						break;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new AttachmentNotSavedException(e.getMessage(), e);
		}

		return ConversionUtil.convertToRepresentation(obsSaver.getObs(),
				new CustomRepresentation(AttachmentsConstants.REPRESENTATION_OBS));
	}

	@RequestMapping(value = AttachmentsConstants.DOWNLOAD_ATTACHMENT_URL, method = RequestMethod.GET)
	public void downloadDocument(@RequestParam("obs") String obsUuid,
			@RequestParam(value = "view", required = false) String view, HttpServletResponse response) {
		if (StringUtils.isEmpty(view))
			view = AttachmentsConstants.ATT_VIEW_ORIGINAL;

		// Getting the Core/Platform complex data object
		Obs obs = context.getObsService().getObsByUuid(obsUuid);
		ComplexData complexData = obs.getComplexData();

		// Switching to our complex data object
		ValueComplex valueComplex = new ValueComplex(obs.getValueComplex());
		AttachmentComplexData docComplexData = context.getComplexDataHelper().build(valueComplex.getInstructions(),
				complexData);

		String mimeType = docComplexData.getMimeType();
		try {
			// The document meta data is sent as HTTP headers.
			response.setContentType(mimeType);
			response.addHeader("Content-Family", getContentFamily(mimeType).name()); // custom header
			response.addHeader("File-Name", docComplexData.getTitle()); // custom header
			response.addHeader("File-Ext", getExtension(docComplexData.getTitle(), mimeType)); // custom header
			response.addHeader("Content-Disposition", "attachment");
			switch (getContentFamily(mimeType)) {
				default :
					response.getOutputStream().write(docComplexData.asByteArray());
					break;
			}
		} catch (IOException e) {
			response.setStatus(500);
			log.error("Could not write to HTTP response for when fetching obs with" + " VALUE_COMPLEX='"
					+ obs.getValueComplex() + "'," + " OBS_ID='" + obs.getId() + "'," + " OBS_UUID='"
					+ obs.getUuid() + "'", e);
		}
	}

	public static String getExtension(String fileName, String mimeType) {
		String ext = FilenameUtils.getExtension(fileName);
		String extFromMimeType = AttachmentsContext.getExtension(mimeType);
		if (!StringUtils.isEmpty(ext)) {
			if (ext.length() > 6) { // this is a bit arbitrary, just to discriminate funny named files such as
				// "uiohdz.iuhezuidhuih"
				ext = extFromMimeType;
			}
		} else {
			ext = extFromMimeType;
		}
		return ext;
	}
}

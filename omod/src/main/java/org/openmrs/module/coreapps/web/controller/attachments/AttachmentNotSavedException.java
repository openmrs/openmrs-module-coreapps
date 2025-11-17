package org.openmrs.module.coreapps.web.controller.attachments;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AttachmentNotSavedException extends RuntimeException {

	private static final long serialVersionUID = -2820372462268285747L;

	public AttachmentNotSavedException(String message, Throwable cause) {
		super(message, cause);
	}
}

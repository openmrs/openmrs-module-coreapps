/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.coreapps.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.web.WebConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter checks if an authenticated user trying to access administrative functions has the
 * <STRONG>System Administration<STRONG> privelege. It will intercept any requests with *admin/* in
 * its url. Unauthorised user will be redirected to the home page.
 */
public class AdminAuthorisationFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(AdminAuthorisationFilter.class);
		
	private static final String COREAPPS_SYSTEM_ADMINISTRATOR_PRIVELEGE = "App: coreapps.systemAdministration";
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		
	}
	
	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		User user = Context.getAuthenticatedUser();
		if (user != null && !user.hasPrivilege(COREAPPS_SYSTEM_ADMINISTRATOR_PRIVELEGE)) {
			httpReq.getSession().setAttribute(WebConstants.DENIED_PAGE, httpReq.getRequestURI());
			HttpServletResponse httpRes = (HttpServletResponse) res;
			log.info("User " + user + " has no privilage \"" + COREAPPS_SYSTEM_ADMINISTRATOR_PRIVELEGE + "\"");
			httpRes.sendRedirect(httpReq.getContextPath() + "/login.htm");
		}
		chain.doFilter(req, res);
	}
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		
	}
}
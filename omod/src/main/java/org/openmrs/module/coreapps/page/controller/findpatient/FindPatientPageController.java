package org.openmrs.module.coreapps.page.controller.findpatient;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FindPatientPageController {
	
	/**
	 * This page is built to be shared across multiple apps. To use it, you must pass an "app"
	 * request parameter, which must be the id of an existing app that is an instance of
	 * coreapps.template.findPatient
	 * 
	 * @param model
	 * @param app
	 * @param sessionContext
	 */
	public void get(PageModel model, @RequestParam("app") AppDescriptor app, UiSessionContext sessionContext,
                    UiUtils ui) {
        model.addAttribute("afterSelectedUrl", app.getConfig().get("afterSelectedUrl").getTextValue());
        model.addAttribute("heading", app.getConfig().get("heading").getTextValue());
        model.addAttribute("label", app.getConfig().get("label").getTextValue());
        model.addAttribute("showLastViewedPatients", app.getConfig().get("showLastViewedPatients").getBooleanValue());

        JsonNode breadcrumbsConfig = app.getConfig().get("breadcrumbs");
        if (!breadcrumbsConfig.isNull()) {
            ObjectMapper jackson = new ObjectMapper();
            if (!breadcrumbsConfig.isArray()) {
                throw new IllegalStateException("breadcrumbs defined in " + app + " must be null or an array");
            }
            List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
            for (JsonNode item : breadcrumbsConfig) {
                Breadcrumb breadcrumb = jackson.convertValue(item, Breadcrumb.class);

                // link needs to have /contextpath prepended
                if (breadcrumb.getLink() != null) {
                    breadcrumb.setLink("/" + WebConstants.CONTEXT_PATH + breadcrumb.getLink());
                }

                // label should be a message code
                if (breadcrumb.getLabel() != null) {
                    breadcrumb.setLabel(ui.message(breadcrumb.getLabel()));
                }
                breadcrumbs.add(breadcrumb);
            }
            model.addAttribute("breadcrumbs", ui.toJson(breadcrumbs));
        } else {
            model.addAttribute("breadcrumbs", null);
        }
	}

    public static class Breadcrumb {

        @JsonProperty
        private String icon;

        @JsonProperty
        private String link;

        @JsonProperty
        private String label;

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

}

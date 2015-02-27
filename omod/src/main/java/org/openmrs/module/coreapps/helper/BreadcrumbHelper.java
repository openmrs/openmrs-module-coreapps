package org.openmrs.module.coreapps.helper;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.ui.framework.page.PageModel;

import java.util.ArrayList;
import java.util.List;

public class BreadcrumbHelper {

    public static void addBreadcrumbsIfDefinedInApp(AppDescriptor app, PageModel model, UiUtils ui) {

        String currentUrl = ui.thisUrlWithContextPath();
        if (currentUrl.charAt(currentUrl.length()-1) == '&') {
            currentUrl = currentUrl.substring(0, currentUrl.length()-1);
        }

        JsonNode breadcrumbsConfig = (app != null && app.getConfig() != null) ? app.getConfig().get("breadcrumbs") : null;
        if (breadcrumbsConfig != null && !breadcrumbsConfig.isNull()) {
            ObjectMapper jackson = new ObjectMapper();
            if (!breadcrumbsConfig.isArray()) {
                throw new IllegalStateException("breadcrumbs defined in " + app + " must be null or an array");
            }
            List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
            int breadcrumbsConfigSize = breadcrumbsConfig.size() -1;
            for (int i=0; i <= breadcrumbsConfigSize ; i++) {
                JsonNode item = breadcrumbsConfig.get(i);
                Breadcrumb breadcrumb = jackson.convertValue(item, Breadcrumb.class);

                // link needs to have /contextpath prepended
                if (breadcrumb.getLink() != null) {
                    breadcrumb.setLink("/" + WebConstants.CONTEXT_PATH + breadcrumb.getLink());
                }

                // label should be a message code
                if (breadcrumb.getLabel() != null) {
                    breadcrumb.setLabel(ui.message(breadcrumb.getLabel()));
                }
                //if this is the last breadcrumb add the current page URL
                if (i == breadcrumbsConfigSize ) {
                    breadcrumb.setLink(currentUrl);
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

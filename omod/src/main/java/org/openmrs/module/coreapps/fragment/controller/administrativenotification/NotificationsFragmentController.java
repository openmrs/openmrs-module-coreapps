package org.openmrs.module.coreapps.fragment.controller.administrativenotification;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.module.appframework.domain.AdministrativeNotification;
import org.openmrs.module.appframework.service.AdministrativeNotificationService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragmentController {

    public void get(@SpringBean AdministrativeNotificationService service,
                    UiSessionContext sessionContext,
                    FragmentModel model) {
        List<AdministrativeNotification> notifications = new ArrayList<AdministrativeNotification>();
        for (AdministrativeNotification candidate : service.getAdministrativeNotifications()) {
            if (StringUtils.isEmpty(candidate.getRequiredPrivilege()) ||
                    sessionContext.getCurrentUser().hasPrivilege(candidate.getRequiredPrivilege())) {
                notifications.add(candidate);
            }
        }

        model.addAttribute("notifications", notifications);
    }

}

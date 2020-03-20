package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.List;
import java.util.Map;

public class ActiveDrugOrdersFragmentController {

    public void controller(FragmentConfiguration config,
                           @FragmentParam("app") AppDescriptor app,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean("orderService") OrderService orderService,
                           FragmentModel model) throws Exception {
        // the coreapps patient page only provides patientId for this extension point
        config.require("patient|patientId");
        Patient patient;
        Object pt = config.getAttribute("patient");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode appConfig = app.getConfig();
        if (appConfig == null) {
        	appConfig = new ObjectMapper().createObjectNode();
        }
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        }
        else {
            // in case we are passed a PatientDomainWrapper
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }

        OrderType drugOrders = orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
        List<Order> activeDrugOrders = orderService.getActiveOrders(patient, drugOrders, null, null);
        Map<String, Object> appConfigMap = mapper.convertValue(appConfig, Map.class);
        // retrieving widget configuration and adding them to the model
        Boolean displayActivationDate = (Boolean) (appConfigMap.get("displayActivationDate") != null ? appConfigMap.get("displayActivationDate") : false);
        String detailsUrl = (String) appConfigMap.get("detailsUrl");
        model.addAttribute("displayActivationDate", displayActivationDate);
        model.addAttribute("detailsUrl", detailsUrl);
        
        model.addAttribute("patient", patient);
        model.addAttribute("activeDrugOrders", activeDrugOrders);
    }
}

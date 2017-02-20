package org.openmrs.module.coreapps.page.controller.providermanagement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.account.AccountDomainWrapper;
import org.openmrs.module.emrapi.account.AccountService;
import org.openmrs.module.emrapi.account.AccountValidator;
import org.openmrs.module.providermanagement.Provider;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.module.providermanagement.exception.InvalidRelationshipTypeException;
import org.openmrs.module.providermanagement.exception.PersonIsNotProviderException;
import org.openmrs.module.providermanagement.exception.SuggestionEvaluationException;
import org.openmrs.module.uicommons.UiCommonsConstants;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.BindParams;
import org.openmrs.ui.framework.annotation.MethodParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.context.MessageSource;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EditProviderPageController {

    protected final Log log = LogFactory.getLog(getClass());

    class ProviderPersonRelationship {
        Person person = null;
        String identifier = null;
        Integer objectId = null;
        Relationship relationship= null;
        RelationshipType relationshipType = null;

        public ProviderPersonRelationship() {}

        public ProviderPersonRelationship(Person person,
                                          String identifier,
                                          Integer objectId,
                                          Relationship relationship,
                                          RelationshipType relationshipType) {
            this.person = person;
            this.identifier = identifier;
            this.objectId = objectId;
            this.relationship = relationship;
            this.relationshipType = relationshipType;
        }
    }

    public AccountDomainWrapper getAccount(@RequestParam(value = "personId", required = false) Person person,
                                           @SpringBean("accountService") AccountService accountService) {

        AccountDomainWrapper account;

        if (person == null) {
            account = accountService.getAccountByPerson(new Person());
        } else {
            account = accountService.getAccountByPerson(person);
            if (account == null)
                throw new APIException("Failed to find user account matching person with id:" + person.getPersonId());
        }

        return account;
    }

    public void get(PageModel model,
                    @MethodParam("getAccount") AccountDomainWrapper account,
                    @ModelAttribute("patientId") @BindParams Patient patient,
                    @SpringBean("patientService") PatientService patientService,
                    @SpringBean("accountService") AccountService accountService,
                    @SpringBean("adminService") AdministrationService administrationService,
                    @SpringBean("providerManagementService") ProviderManagementService providerManagementService)
            throws PersonIsNotProviderException, InvalidRelationshipTypeException, SuggestionEvaluationException {

        model.addAttribute("account", account);
        model.addAttribute("providerRoles", providerManagementService.getAllProviderRoles(false));

        List<ProviderPersonRelationship> patientsList = new ArrayList<ProviderPersonRelationship>();
        List<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
        Set<ProviderAttributeType> providerAttributeTypes = new HashSet<ProviderAttributeType>();
        List<ProviderPersonRelationship> supervisorsForProvider = null;

        Provider provider = account.getProvider();
        if (provider != null ) {
            supervisorsForProvider = getSupervisors(provider, providerManagementService);
            ProviderRole providerRole = provider.getProviderRole();
            if (providerRole != null && providerRole.getRelationshipTypes() != null) {
                providerAttributeTypes = providerRole.getProviderAttributeTypes();
                Set<ProviderAttribute> attributes = provider.getAttributes();
                for (ProviderAttribute attribute : attributes) {
                    // remove from the list of Attribute Types the ones that are already entered for this provider
                    providerAttributeTypes.remove(attribute.getAttributeType());
                }
                for (RelationshipType relationshipType : provider.getProviderRole().getRelationshipTypes() ) {
                    if (!relationshipType.isRetired()) {
                        relationshipTypes.add(relationshipType);
                    }
                }
                patientsList= getAssignedPatients(provider, providerManagementService, patientService);
            }
        }
        model.addAttribute("relationshipTypes", relationshipTypes);
        model.addAttribute("patientsList", patientsList);
        model.addAttribute("providerAttributeTypes", providerAttributeTypes);
        model.addAttribute("supervisorsForProvider", supervisorsForProvider);
    }

    public String post(@MethodParam("getAccount") @BindParams AccountDomainWrapper account, BindingResult errors,
                       @RequestParam(value = "userEnabled", defaultValue = "false") boolean userEnabled,
                       @RequestParam(value = "providerIdentifier", required = false) String providerIdentifier,
                       @ModelAttribute("personAddress") @BindParams PersonAddress address,
                       @SpringBean("providerService") ProviderService providerService,
                       @SpringBean("messageSource") MessageSource messageSource,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                       @SpringBean("accountService") AccountService accountService,
                       @SpringBean("adminService") AdministrationService administrationService,
                       @SpringBean("providerManagementService") ProviderManagementService providerManagementService,
                       @SpringBean("accountValidator") AccountValidator accountValidator, PageModel model,
                       HttpServletRequest request) {

        accountValidator.validate(account, errors);

        Map<Integer, String> attributesMap = getAttributeMap("providerAttributeId_", request);
        Map<Integer, String> attributeTypesMap = getAttributeMap("attributeTypeId_", request);

        if (!errors.hasErrors()) {
            try {
                Person person = account.getPerson();
                if (address != null ) {
                    person.addAddress(address);
                }
                Provider provider = account.getProvider();

                if (StringUtils.isNotBlank(providerIdentifier)) {
                    provider.setIdentifier(providerIdentifier);
                }
                if ( attributesMap.size() > 0 ) {
                    for (Integer id : attributesMap.keySet()) {
                        ProviderAttribute providerAttribute = providerService.getProviderAttribute(id);
                        if (providerAttribute != null) {
                            providerAttribute.setValueReferenceInternal(attributesMap.get(id));
                        }
                    }
                }
                if (attributeTypesMap.size() > 0 ) {
                    for (Integer typeId : attributeTypesMap.keySet()) {
                        ProviderAttributeType providerAttributeType = providerService.getProviderAttributeType(typeId);
                        if ( providerAttributeType != null ) {
                            ProviderAttribute attr = new ProviderAttribute();
                            attr.setAttributeType(providerAttributeType);
                            attr.setValueReferenceInternal(attributeTypesMap.get(typeId));
                            provider.addAttribute(attr);
                        }
                    }
                }
                accountService.saveAccount(account);
                request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                        messageSourceService.getMessage("Provider saved"));
                request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

                return "redirect:/coreapps/providermanagement/editProvider.page?personId=" + person.getId();
            } catch (Exception e) {
                log.warn("Some error occurred while saving account details:", e);
                request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
                        messageSourceService.getMessage("Failed to save provider", new Object[]{e.getMessage()}, Context.getLocale()));
            }
        } else {
            sendErrorMessage(errors, messageSource, request);
        }

        model.addAttribute("errors", errors);
        model.addAttribute("account", account);
        model.addAttribute("providerRoles", providerManagementService.getAllProviderRoles(false));

        return "redirect:/coreapps/providermanagement/editProvider.page";
    }

    private List<ProviderPersonRelationship> getAssignedPatients(Provider provider,
                                                                 ProviderManagementService providerManagementService,
                                                                 PatientService patientService)
            throws InvalidRelationshipTypeException, PersonIsNotProviderException {

        List<ProviderPersonRelationship> patientsList = new ArrayList<ProviderPersonRelationship>();
        for (RelationshipType relationshipType : provider.getProviderRole().getRelationshipTypes() ) {
            if (!relationshipType.isRetired()) {
                for (Relationship relationship : providerManagementService.getPatientRelationshipsForProvider(provider.getPerson(), relationshipType, null)) {
                    if (relationship.getPersonB().isPatient()) {
                        Patient temp = patientService.getPatient(relationship.getPersonB().getId());
                        patientsList.add(new ProviderPersonRelationship(
                                    temp,
                                    temp.getPatientIdentifier().getIdentifier(),
                                    temp.getPatientId(),
                                    relationship,
                                    relationshipType));
                    }
                }
            }
        }
        return patientsList;
    }

    private List<ProviderPersonRelationship> getSupervisors(Provider provider, ProviderManagementService providerManagementService)
            throws PersonIsNotProviderException {
        List<ProviderPersonRelationship> supervisors = new ArrayList<ProviderPersonRelationship>();
        Person person = provider.getPerson();
        List<Person> supervisorPersons = providerManagementService.getSupervisorsForProvider(person);
        for (Person supervisor : supervisorPersons) {
            List<Provider> providersByPerson = providerManagementService.getProvidersByPerson(supervisor, true);
            if (providersByPerson !=null && providersByPerson.size() > 0) {
                RelationshipType supervisorRelationshipType = providerManagementService.getSupervisorRelationshipType();
                Relationship supervisorRelationship = null;
                Provider supervisorProvider = providersByPerson.get(0);
                List<Relationship> relationships = Context.getPersonService().getRelationships(supervisor,
                        person, supervisorRelationshipType, null);
                if (relationships != null && relationships.size() > 0 ){
                    supervisorRelationship = relationships.get(0);
                }
                supervisors.add(new ProviderPersonRelationship(
                        supervisorProvider.getPerson(),
                        supervisorProvider.getIdentifier(),
                        supervisor.getId(),
                        supervisorRelationship,
                        supervisorRelationshipType));
            }
        }
        return supervisors;
    }

    private Map<Integer, String> getAttributeMap(String parameterPrefix, HttpServletRequest request) {
        Map<Integer, String> attributesMap = new HashMap<Integer, String>();
        Set<String> paramKeys = request.getParameterMap().keySet();
        for (String param : paramKeys) {
            if (param.startsWith(parameterPrefix)) {
                Integer providerAttributeId = Integer.valueOf(param.substring(parameterPrefix.length()));
                String providerAttributeValue = request.getParameter(param);
                if ((providerAttributeId != null) &&
                        (providerAttributeId.intValue() > 0) &&
                        StringUtils.isNotBlank(providerAttributeValue)) {
                    attributesMap.put(providerAttributeId, providerAttributeValue);
                }
            }
        }
        return attributesMap;
    }

    private void sendErrorMessage(BindingResult errors, MessageSource messageSource, HttpServletRequest request) {
        List<ObjectError> allErrors = errors.getAllErrors();
        String message = getMessageErrors(messageSource, allErrors);
        request.getSession().setAttribute(UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
                message);
    }

    private String getMessageErrors(MessageSource messageSource, List<ObjectError> allErrors) {
        String message = "";
        for (ObjectError error : allErrors) {
            Object[] arguments = error.getArguments();
            String errorMessage = messageSource.getMessage(error.getCode(), arguments, Context.getLocale());
            message = message.concat(replaceArguments(errorMessage, arguments).concat("<br>"));
        }
        return message;
    }

    private String replaceArguments(String message, Object[] arguments) {
        if (arguments != null) {
            for (int i = 0; i < arguments.length; i++) {
                String argument = (String) arguments[i];
                message = message.replaceAll("\\{" + i + "\\}", argument);
            }
        }
        return message;
    }
}

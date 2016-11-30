package org.openmrs.module.coreapps.page.controller.relationships;

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;

import java.util.Date;


public class RelativeDetailsPageController {

	public Redirect get(@RequestParam("personId") String personId,
			@RequestParam(required = false, value = "app") AppDescriptor app,
			PageModel model) {
		Person person = Context.getPersonService().getPersonByUuid(personId);

		model.addAttribute("app", app);
		model.addAttribute("person", person);
		model.addAttribute("ageInDays", this.getAgeInDays(person));
		model.addAttribute("ageInMonths", this.getAgeInMonths(person));


		return null;
	}


	public Integer getAgeInMonths(Person person) {
		if (person.getBirthdate() == null) {
			return null;
		}
		Date endDate = person.isDead() ? person.getDeathDate() : new Date();
		return Months.monthsBetween(new DateTime(person.getBirthdate()), new DateTime(endDate)).getMonths();
	}
	public Integer getAgeInDays(Person person) {
		if (person.getBirthdate() == null) {
			return null;
		}
		Date endDate = person.isDead() ? person.getDeathDate() : new Date();
		return Days.daysBetween(new DateTime(person.getBirthdate()), new DateTime(endDate)).getDays();
	}

}

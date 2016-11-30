<span class="gender-age">
    <span>${ui.message("coreapps.gender." + person.gender)}&nbsp;</span>
    <span>
    <% if (person.birthdate) { %>
    <% if (person.age > 0) { %>
        ${ui.message("coreapps.ageYears", person.age)} 
    <% } else if (ageInMonths > 0) { %>
        ${ui.message("coreapps.ageMonths", ageInMonths)}
    <% } else { %>
        ${ui.message("coreapps.ageDays", ageInDays)}
    <% } %>   
    (<% if (person.birthdateEstimated) { %>~<% } %>${ ui.formatDatePretty(person.birthdate) })          
    <% } else { %>
        ${ui.message("coreapps.unknownAge")}
    <% } %>
    </span>
</span>

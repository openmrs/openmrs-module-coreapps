<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("providermanagement.providerList")}"}
    ];

    jq(function() {

        setTimeout(function() {
            jq("#providers-list_filter input:text").first().focus();
        }, 500);
    });

</script>

<h2>${ ui.message("Provider.manage") }</h2>

<a href="${ ui.pageLink("coreapps", "providermanagement/findPatient") }">
    <button id="create-account-button">${ ui.message("coreapps.provider.createProviderAccount") }</button>
</a>
<hr>

<table id="providers-list" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("providermanagement.identifier") }</th>
        <th>${ ui.message("coreapps.person.name") }</th>
        <th>${ ui.message("coreapps.gender") }</th>
        <th>${ ui.message("Role.role") }</th>
        <th>${ ui.message("providermanagement.supervisor") }</th>
    </tr>
    </thead>

    <tbody>
    <% if ((providersList == null) ||
            (providersList != null && providersList.size() == 0)) { %>
    <tr>
        <td colspan="5">${ ui.message("coreapps.none") }</td>
    </tr>
    <% } %>
    <% providersList.each { provider, supervisor ->
        def personId = provider.person.personId
        def personName = provider.name
        def mySupervisor = null
        supervisor.each {
            // retrieve the most recent active supervisor
            if (it.relationship && it.relationship.endDate == null) {
                mySupervisor = it
            }
        }
    %>
    <tr id="provider-${ provider.person.personId}">
        <td>${ ui.format(provider.identifier) }</td>
        <td><a href="/${ contextPath }/coreapps/providermanagement/editProvider.page?personId=${ provider.person.personId }">${ ui.format(provider.name) }</a></td>
        <td>${ ui.format(provider.person.gender) }</td>
        <td>${ ui.format(provider.providerRole) }</td>
        <td>
        <% if (mySupervisor && mySupervisor.relationship && (mySupervisor.relationship.endDate == null) ) { %>
            <a href="/${ contextPath }/coreapps/providermanagement/editProvider.page?personId=${ mySupervisor.person.id }">${ ui.format(mySupervisor.person.personName) }</a>
        <% } %>
        </td>

    </tr>
    <% } %>
    </tbody>
</table>

<% if ( (providersList != null) && (providersList.size() > 0) ) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#providers-list",
                                                         options: [
                                                                 bFilter: true,
                                                                 bJQueryUI: true,
                                                                 bLengthChange: false,
                                                                 iDisplayLength: 20,
                                                                 sPaginationType: '\"full_numbers\"',
                                                                 bSort: false,
                                                                 sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
                                                         ]
]) }
<% } %>
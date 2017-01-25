<%
    ui.includeCss("coreapps", "visit/visits.css")
%>
<% if (visitTypes.size > 1) { %>
<div id="edit-visit-dialog-${ config.visit.id }" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-check-in"></i>
        <h3>${ ui.message("coreapps.task.editVisit.label")}</h3>
    </div>
    <div class="dialog-content form">
        <form id="edit-visit-dialog-form-${ config.visit.id }">
            <input type="hidden" id="patientId" value="${ config.visit.patient.id }"/>
            <input type="hidden" id="selectedTypeId" />
            <input type="hidden" id="dateFormat" value='<%= org.openmrs.api.context.Context.getDateFormat().toPattern().toLowerCase() %>' />
            <table class="left-aligned-th edit-visit-table">
                <tr>
                    <td><label>${ ui.message("coreapps.task.visitType.label") }:</label></td>
                    <td>
                        <select id="visit-type-drop-down" onchange="visit.setSelectedDropdownValue('selectedTypeId', this)">
                            <option class="dialog-drop-down small">${ui.message("coreapps.task.visitType.label")}</option>
                            <% visitTypes.each { type -> %>
                            <% if (visit.visitType == type) { %>
                            <option class="dialog-drop-down small" value ="${type.id}" selected="true">${ ui.format(type) }</option>
                            <% } else {%>
                            <option class="dialog-drop-down small" value ="${type.id}">${ ui.format(type) }</option>
                            <%  }
                            } %>
                        </select>
                    </td>
                </tr>

                <!-- visit attributes -->
                <% visitAttributeTypes.each { type -> %>
                <% if(type.retired == false){ %>
                <tr>
                    <td class="info">${type.name}: </td>
                    <td>
                        <%
                                def elementFormName;
                                def value = '';
                                def typeId = '';
                                visit.activeAttributes.each{ vtype ->
                                    if(type.uuid == vtype.attributeType.uuid) {
                                        value = vtype.valueReference;
                                        typeId = vtype.id;
                                    }
                                }

                                if(typeId == '') {
                                    elementFormName = 'attribute.' + type.id + '.new[' + type.id + ']';
                                } else {
                                    elementFormName = 'attribute.' + type.id + '.existing[' + typeId + ']';
                                }
                        %>
                        <% if(type.datatypeClassname == 'org.openmrs.customdatatype.datatype.BooleanDatatype'){ %>
                        <div>
                            <input type="radio" value="false" name="${elementFormName}" <% if(value == 'false'){ %> checked <% } %> /> ${ui.message("False")}
                        </div>
                        <div>
                            <input type="radio" value="true" name="${elementFormName}" <% if(value == 'true'){ %> checked <% } %> />  ${ui.message("True")}
                        </div>
                        <% } else if(type.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype'){ %>
                        ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                formFieldName: elementFormName + ".date",
                                label: "<span class='left' style='font-size:11px'>" + ui.message('coreapps.task.existingDate.label') + ": " + value + "</span>",
                                useTime: false
                        ])}
                        <% } else if(type.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.CodedConceptDatatype'){ %>
                        <select id="coded-data-types" name="${elementFormName}"></select>
                        <script>
                            var conceptId = '${type.datatypeConfig}';
                            visit.getCodedConcepts(conceptId, '${elementFormName}', '${value}', '${ config.visit.id }');
                        </script>
                        <% } else { %>
                        <input type="text" size="15" name="${elementFormName}" value="${value}" />
                        <% } %>
                    </td>
                </tr>
                <% } %>
                <% } %>
            </table>
            <br><br>
            <button class="cancel">${ ui.message("coreapps.cancel") }</button>
            <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        </form>
    </div>
</div>
<% } %>
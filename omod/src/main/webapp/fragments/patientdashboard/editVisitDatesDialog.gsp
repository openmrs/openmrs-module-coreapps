<div id="edit-visit-dates-dialog-${ config.visitId }" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-check-in"></i>
        <h3>${ ui.message("coreapps.task.editVisitDate.label") }</h3>
    </div>
    <div class="dialog-content form">
        <form id="edit-visit-dates-dialog-form-${ config.visitId }">
            <input type="hidden" name="visitId" value="${ config.visitId }"/>
            <p>
                <label for="startDate" class="required">
                    ${ ui.message("coreapps.startDate.label") }
                </label>

                ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                        id: "startDate" + config.visitId,
                        formFieldName: "startDate",
                        label:"",
                        useTime: false,
                        startDate: config.startDateLowerLimit,
                        endDate: config.startDateUpperLimit,
                        defaultDate: config.defaultStartDate
                ])}
            </p>
            <% if(config.defaultEndDate != null) { %>
                <p>
                    <label for="stopDate" class="required">
                        ${ ui.message("coreapps.stopDate.label") }
                    </label>

                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "stopDate" + config.visitId,
                            formFieldName: "stopDate",
                            label:"",
                            useTime: false,
                            startDate: config.endDateLowerLimit,
                            endDate: config.endDateUpperLimit,
                            defaultDate: config.defaultEndDate
                    ])}
                </p>
            <% } %>

            <br><br>

            <button class="cancel">${ ui.message("coreapps.cancel") }</button>
            <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        </form>
    </div>
</div>
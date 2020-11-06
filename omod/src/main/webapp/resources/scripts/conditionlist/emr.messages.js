(function () {
    jQuery(function () {
        if (typeof emr !== "undefined") {
            emr.loadMessages([
                "coreapps.conditionui.getCondition.failure",
                "coreapps.conditionui.updateCondition.success",
                "coreapps.conditionui.updateCondition.added",
                "coreapps.conditionui.updateCondition.edited",
                "coreapps.conditionui.updateCondition.error",
                "coreapps.conditionui.updateCondition.onSetDate.error",
                "coreapps.conditionui.removeCondition.success",
                "coreapps.conditionui.removeCondition.message"
            ]);
        }
    });
})();

<%
    def filterActions = { actions ->
        if (!actions) { return [] };
        return actions.findAll {
            !it.requiredPrivilege || context.hasPrivilege(it.requiredPrivilege)
        }
    }

    def actionHref = { act ->
        if (act.url) {
            return "/" + ui.contextPath() + act.url;
        }
        else if (act.script) {
            return "javascript:" + act.script;
        }
        else {
            return "#";
        }
    }
%>
<% if (notifications) { %>
    <div class="note-container">
        <% notifications.each { notification ->
            def actions = filterActions(notification.actions)
        %>
            <div class="note ${notification.cssClass?:''}">
                <div class="text">
                    <% if (notification.icon) { %>
                        <i class="${notification.icon} medium"></i>
                    <% } %>
                    <p>${ ui.message(notification.label) }</p>
                    <% if (notification.actions) { %>
                        <p>
                            <% notification.actions.each { act -> %>
                                <a href="${actionHref(act)}">${ui.message(act.label)}</a>
                            <% } %>
                        </p>
                    <% } %>
                </div>
            </div>
        <% } %>
    </div>
<% } %>
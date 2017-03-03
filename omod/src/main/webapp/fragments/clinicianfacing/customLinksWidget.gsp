<div id="coreapps-customLinks" class="info-section">
    <div class="info-header">
        <i class="${config.icon}"></i>
        <h3>${ ui.message(config.label).toUpperCase() }</h3>
    </div>
    <div class="info-body">
        <ul>
            <% config.links.each { title, link -> %>
            <li><a href="${link}">${title}</a> </li>
            <% } %>
        </ul>
    </div>
</div>

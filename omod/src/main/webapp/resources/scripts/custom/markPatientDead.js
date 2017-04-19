/**
 * Show the container, and enable all elements in it
 * @param containerId
 */
function showContainer(containerId) {
    jq(containerId).removeClass('hidden');
    jq(containerId + ' :input').attr('disabled', false);
    jq(containerId + ' :input').prop('checked', false);
}

/**
 * Hide the container, and disables all elements in it
 * @param containerId
 */
function hideContainer(containerId) {
    jq(containerId).addClass('hidden');
    jq(containerId + ' :input').attr('disabled', true);
    jq(containerId + ' :input').prop('checked', false);
}
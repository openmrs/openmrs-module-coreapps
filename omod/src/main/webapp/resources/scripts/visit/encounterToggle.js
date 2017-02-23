/**
 *  Provide functionality to show or hide ALL encounter details.
 */
function toggle(){
    jq('.collapse').removeClass('open');
    jq(".view-details").click();
    if(jq('#i-toggle').hasClass('icon-arrow-up')){
        jq('#i-toggle').removeClass('icon-arrow-up');
        jq('#i-toggle').addClass('icon-arrow-down');
        jq('#i-toggle').attr('title', "${ui.message('coreapps.showAllEncounterDetails')}");
    } else {
        jq('#i-toggle').removeClass('icon-arrow-down');
        jq('#i-toggle').addClass('icon-arrow-up');
        jq('#i-toggle').attr('title', "${ui.message('coreapps.hideAllEncounterDetails')}");
    }
}
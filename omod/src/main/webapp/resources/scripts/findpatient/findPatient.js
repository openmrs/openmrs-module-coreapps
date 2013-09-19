function PatientSearchWidget(configuration){
    var defaults = {
        minSearchCharacters: 3,
        searchInputId: 'patient-search',
        searchResultsDivId: 'patient-search-results'
    };

    var config = jq.extend({}, defaults, configuration);
    var tableId = 'patient-search-results-table';
    var tableHtml = '<table id="'+tableId+'">'+
                        '<thead>'+
                            '<tr>'+
                                '<th>'+config.messages.identifierColHeader+'</th>'+
                                '<th>'+config.messages.nameColHeader+'</th>'+
                                '<th>'+config.messages.genderColHeader+'</th>'+
                                '<th>'+config.messages.ageColHeader+'</th>'+
                                '<th>'+config.messages.birthdateColHeader+'</th>'+
                            '</tr>'+
                        '</thead>'+
                        '<tbody></tbody>'+
                    '</table>';

    var searchingHtml = '<span><img class="search-spinner" src="/' + OPENMRS_CONTEXT_PATH+'/ms/uiframework/resource/uicommons/images/spinner.gif" />'+config.messages.searching+'...</span>';

    jq('#'+config.searchResultsDivId).append(tableHtml);
    var input = jq('#'+config.searchInputId);
    var pageCount = 0;
    var searchResultsData = [];
    var highlightedKeyboardRowIndex;
    var highlightedMouseRowIndex;
    var searchDelayShort = 300;
    var searchDelayLong = 1000;
    var searchDelayTimer;
    var url = '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/patient';
    var initialData = [];
    var initialPatientUuids = [];
    var tableObject = jq('#'+tableId);
    if(config.initialPatients){
        _.each(config.initialPatients, function(p){
            //only add the uuid since it is only one we need to reference later
            searchResultsData.push({uuid: p.uuid});
            initialPatientUuids.push(p.uuid);
            initialData.push([p.identifier+" <span class='recent-lozenge'>"+config.messages.recent+"</span>",
                p.fullName, p.gender, p.age, p.birthdate]);
        });
    }
    if(initialPatientUuids.length > 0){
         jq('#'+config.searchResultsDivId).show();
    }
    //UP(38), DOWN(40), PAGE UP(33), PAGE DOWN(34)
    var keyboardNavigationKeys = [33,34,38,40];
    /*
     *SHIFT(16), CTRL(17), ALT(18), CAPS LOCK(20), ESC(27), END(35), HOME(36), LEFT(37), RIGHT(39)
     *MAC COMMAND KEYs differ by browser
     *  Firefox: 224
     *  Opera: 17(CTRL equivalent)
     *  WebKit (Safari/Chrome): 91 (Left command key) or 93 (Right command key)
     */
    var keyboardControlKeys = [16,17,18,20,27,35,36,37,39,91,93,224];
    var customRep = 'custom:(uuid,' +
                    'patientIdentifier:(uuid,identifier),' +
                    'person:(gender,age,birthdate,personName:(fullName)))';

    var doSearch = function(query){
        reset();
        jq('#'+tableId).find('td.dataTables_empty').html(searchingHtml);
        if(!jq('#'+config.searchResultsDivId).is(':visible')){
            jq('#'+config.searchResultsDivId).show();
        }
        query = jq.trim(query);
        jq.getJSON(url, {v: customRep, q: query }, function(data) {
            jq('#'+tableId).find('td.dataTables_empty').html(' ');
            if(data){
                updateSearchResults(data.results);
            }
        });
    }

    var reset = function(){
        dTable.fnClearTable();
        searchResultsData = [];
        pageCount = 0;
        highlightedKeyboardRowIndex = undefined;
        highlightedMouseRowIndex = undefined;
    }

    var updateSearchResults = function(results){
        reset();
        searchResultsData = results;
        var dataRows = [];
        _.each(searchResultsData, function(patient) {
            var birthdate = '';
            if(patient.person.birthdate){
                birthdate = moment(patient.person.birthdate).format('DD-MMM-YYYY');
            }
            var identifier = patient.patientIdentifier.identifier;
            if(_.contains(initialPatientUuids, patient.uuid)){
                identifier = patient.patientIdentifier.identifier+
                    " <span class='recent-lozenge'>"+config.messages.recent+"</span>";
            }
            dataRows.push([identifier, patient.person.personName.fullName,
                patient.person.gender, patient.person.age, birthdate]);
        });

        dTable.fnAddData(dataRows);

        refreshTable();
    }

    var refreshTable = function(){
        var rowCount = searchResultsData.length;
        if(rowCount % dTable.fnSettings()._iDisplayLength == 0){
            pageCount = rowCount/dTable.fnSettings()._iDisplayLength;
        }else{
            pageCount = Math.floor(rowCount/dTable.fnSettings()._iDisplayLength)+1;
        }
    }

    var isTableEmpty = function(){
        if(searchResultsData.length > 0){
            return false
        }
        return !dTable || dTable.fnGetNodes().length == 0;
    }

    var selectRow = function(selectedRowIndex){
        var uuid = searchResultsData[selectedRowIndex].uuid;
        location.href = '/' + OPENMRS_CONTEXT_PATH + emr.applyContextModel(config.afterSelectedUrl, { patientId: uuid});
    }

    var doKeyEnter = function() {
        if (highlightedKeyboardRowIndex == undefined){
            clearTimeoutIfNecessary();
            doSearch(input.val());
            return;
        }
        selectRow(highlightedKeyboardRowIndex);
    }

    var clearTimeoutIfNecessary = function(){
        //if there is any search delay in progress, cancel it
        if(searchDelayTimer != undefined){
            window.clearTimeout(searchDelayTimer);
        }
    }

    var doKeyDown = function() {
        //the user is using the mouse and they also want to use up/down keys?, dont support this
        if(highlightedMouseRowIndex)
            return;

        var prevRow = highlightedKeyboardRowIndex;
        //if we are on the last page, and the last row is highlighted, do nothing
        if(isLastPage() && prevRow >= (searchResultsData.length-1))
            return;

        //only move the highlight to next row if it is currently on the visible page otherwise should be on first row
        if(isHighlightedRowOnVisiblePage()){
            highlightedKeyboardRowIndex++;

            //If the selected row is the first one on the next page, flip over to its page
            if(highlightedKeyboardRowIndex != 0 && (highlightedKeyboardRowIndex % dTable.fnSettings()._iDisplayLength) == 0) {
                dTable.fnPageChange('next');
            }
        }

        if(prevRow != undefined) {
            unHighlightRow(dTable.fnGetNodes()[prevRow]);
        }

        highlightRow();
    }

    var doKeyUp = function() {
        if(highlightedMouseRowIndex)
            return;

        //we are on the first page and the first row is already highlighted, do nothing
        if(dTable.fnSettings()._iDisplayStart == 0 && highlightedKeyboardRowIndex == 0)
            return;

        var prevRow = highlightedKeyboardRowIndex;
        //only move the highlight to prev row if it is currently on the visible page otherwise shoule be last row
        if(isHighlightedRowOnVisiblePage()){
            highlightedKeyboardRowIndex--;
            if(prevRow != undefined) {
                if(prevRow % dTable.fnSettings()._iDisplayLength == 0)
                    dTable.fnPageChange('previous');

                unHighlightRow(dTable.fnGetNodes()[prevRow]);
            }
        }else{
            //user just flipped pages, highlight the last row on the currently visible page
            if(!isLastPage()){
                highlightedKeyboardRowIndex = dTable.fnSettings()._iDisplayStart + dTable.fnSettings()._iDisplayLength - 1;
            }else{
                //this is the last page, highlight the last item in the table
                highlightedKeyboardRowIndex = searchResultsData.length-1;
            }
        }

        highlightRow();
    }

    var doPageDown = function() {
        dTable.fnPageChange('next');
        //if this is the last page and we already have a selected row, do nothing
        if(isLastPage() && isHighlightedRowOnVisiblePage())
            return;

        unHighlightKeyboardSelectedRowIfAny();
    }

    var doPageUp = function() {
        dTable.fnPageChange('previous');
        if(isHighlightedRowOnVisiblePage())
            return;

        unHighlightKeyboardSelectedRowIfAny();
    }

    /**
     * Highlights the row at the index that matches the value of 'highlightedKeyboardRowIndex' otherwise the
     * first on the current visible page
     */
    var highlightRow = function(){
        //always remove highlightfrom  the current keyboard row if any before proceeding
        if(highlightedKeyboardRowIndex != undefined){
            var prevHighlightedRowNode = dTable.fnGetNodes()[highlightedKeyboardRowIndex];
            jq(prevHighlightedRowNode).removeClass('search_table_row_highlight');
        }
        //the row to highlight has to be on the visible page, this helps not to lose the highlight
        //when the user uses the pagination buttons(datatables provides no callback function)
        if(!isHighlightedRowOnVisiblePage()){
            //highlight the first row on the currently visible page
            highlightedKeyboardRowIndex = dTable.fnSettings()._iDisplayStart;
        }
        var highlightedRowNode = dTable.fnGetNodes()[highlightedKeyboardRowIndex];
        jq(highlightedRowNode).addClass('search_table_row_highlight');
    }

    var unHighlightKeyboardSelectedRowIfAny = function(){
        if(highlightedKeyboardRowIndex != undefined){
            unHighlightRow(dTable.fnGetNodes()[highlightedKeyboardRowIndex]);
            highlightedKeyboardRowIndex = undefined;
        }
    }

    /**
     * Removes the highlight from the specified row
     */
    var unHighlightRow = function(row){
        jq(row).removeClass("search_table_row_highlight");
    }

    /**
     * Returns true if the row highlight is on the current visible page
     */
    var isHighlightedRowOnVisiblePage = function(){
        return highlightedKeyboardRowIndex != undefined && (highlightedKeyboardRowIndex >= dTable.fnSettings()._iDisplayStart)
            && (highlightedKeyboardRowIndex < (dTable.fnSettings()._iDisplayStart + dTable.fnSettings()._iDisplayLength));
    }

    /**
     * Gets the current page the user is viewing in the datatable
     */
    var getCurrVisiblePage = function(){
        return Math.ceil(dTable.fnSettings()._iDisplayStart / dTable.fnSettings()._iDisplayLength) + 1;
    }

    var isLastPage = function(){
        return getCurrVisiblePage() == pageCount;
    }

    /********************* INITIALIZATION DATATABLE *********************/

    var dTable = tableObject.dataTable({
        bFilter: false,
        bJQueryUI: true,
        bLengthChange: false,
        iDisplayLength: 15,
        sPaginationType: "full_numbers",
        bSort: false,
        aaData: initialData,
        sDom: 't<"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg"ip>',
        oLanguage: {
            "sInfo": config.messages.info,
            "sZeroRecords": config.messages.noMatchesFound,
            "oPaginate": {
                "sFirst": config.messages.first,
                "sPrevious": config.messages.previous,
                "sNext": config.messages.next,
                "sLast": config.messages.last
            }
        },

        fnDrawCallback : function(oSettings){
            if(isTableEmpty()){
                //this should ensure that nothing happens when the use clicks the
                //row that contain the text that says 'No data available in table'
                return;
            }

            if(highlightedKeyboardRowIndex != undefined && !isHighlightedRowOnVisiblePage()){
                unHighlightRow(dTable.fnGetNodes(highlightedKeyboardRowIndex));
            }

            //fnDrawCallback is called on each page redraw so we need to remove any previous handlers
            //otherwise there will multiple hence the logic getting executed multiples times as the
            //user the goes back and forth between pages
            tableObject.find('tbody tr').unbind('click');
            tableObject.find('tbody tr').unbind('hover');

            tableObject.find('tbody tr').click(
                function(){
                    highlightedMouseRowIndex = dTable.fnGetPosition(this);
                    selectRow(highlightedMouseRowIndex);
                }
            );

            tableObject.find('tbody tr').hover(
                function(){
                    if(highlightedKeyboardRowIndex){
                        var keyboardHighlightedNode = dTable.fnGetNodes()[highlightedKeyboardRowIndex];
                        unHighlightRow(keyboardHighlightedNode);
                        highlightedKeyboardRowIndex = undefined;
                    }
                    highlightedMouseRowIndex = dTable.fnGetPosition(this);
                }, function(){
                    highlightedMouseRowIndex = undefined;
                }
            );

            //ensure that the input never loses field whenever a user
            //e.g when user clicks the paging buttons
            input.focus();
        }
    });

    /***************** SETUP KEYBOARD AND MOUSE EVENT HANDLERS **************/

    input.keyup(function(event) {
        var kc = event.keyCode;
        //ignore enter(because it was handled already onkeydown), keyboard navigation and control keys
        if(kc == 13 || _.contains(keyboardNavigationKeys, kc) || _.contains(keyboardControlKeys, kc)) {
            return false;
        }

        //if there is any search delay set, cancel it
        clearTimeoutIfNecessary();

        var text = jq.trim(input.val());
        if(text.length >= config.minSearchCharacters){
            // force a longer delay since we are going to search on a shorter string
            //or may be the user is still typing more characters
            var effectiveSearchDelay = searchDelayShort;
            if(config.minSearchCharacters < 3 && text.length < 3){
                effectiveSearchDelay = searchDelayLong;
            }

            //wait for a couple of milliseconds, if the user isn't typing anymore chars before triggering search
            //this minimizes the number of un-necessary calls made to the server for first typists
            searchDelayTimer = window.setTimeout(function(){
                doSearch(text);
            }, effectiveSearchDelay);
        }else{
            reset();
        }

        return false;
    });

    //catch control keys to stop the cursor in the input box from moving.
    input.keydown(function(event) {
        var kc = event.keyCode;
        if(kc == 13 ||(_.contains(keyboardNavigationKeys, kc) && !isTableEmpty())) {

            switch(kc) {
                case 13:
                    doKeyEnter();
                    break;
                case 33:
                    doPageUp();
                    break;
                case 34:
                    doPageDown();
                    break;
                case 38:
                    doKeyUp();
                    break;
                case 40:
                    doKeyDown();
                    break;
            }

            return false;
        }

        return true;
    });
}
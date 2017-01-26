window.filter = [];

$(document).ready(function () {
	$(".filter").click(function () {
		var filterValue = $(this).attr("value");

		if (!(window.filter.indexOf(filterValue) < 0)) {
			if ($(this).hasClass("enabled")) {
				$(this).removeClass("enabled");
				$(this).addClass("disabled");
				window.filter.splice(filter.indexOf(filterValue),1);
				$('#active-visits').DataTable().draw();
			}
		} else {
			window.filter.push(filterValue);
			$('#active-visits').DataTable().draw();
			$(this).addClass("enabled");
			$(this).removeClass("disabled");
		}
	})
})

/* Custom filtering function which will filter data in column four */
$(document).ready(function () {
	if($.fn.dataTable){
		$.fn.dataTable.ext.search.push(
			function( settings, data, dataIndex ) {
				filter = window.filter;
				var type = parseFloat( data[4].replace(/\s/g, '') ).toString();
				if ( !(filter.indexOf(type) < 0) || filter == "" )
				{
					return true;
				}
				return false;
			}
		);
	}
});

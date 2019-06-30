var moduleResourceRootPath = "../ms/uiframework/resource/coreapps";
var bmiAgeRevPath = moduleResourceRootPath + "/cdc/csv/bmiagerev.csv";
var hcageinfPath = moduleResourceRootPath + "/cdc/csv/hcageinf.csv";
var lenageinfPath = moduleResourceRootPath + "/cdc/csv/lenageinf.csv";
var statagePath = moduleResourceRootPath + "/cdc/csv/statage.csv";
var wtagePath = moduleResourceRootPath + "/cdc/csv/wtage.csv";
var wtageinfPath = moduleResourceRootPath + "/cdc/csv/wtageinf.csv";
var wtleninfPath = moduleResourceRootPath + "/cdc/csv/wtleninf.csv";
var wtstatPath = moduleResourceRootPath + "/cdc/csv/wtstat.csv";
var bfa_boysPath = moduleResourceRootPath + "/who/csv/bfa_boys_p_exp.csv";
var bfa_girlsPath = moduleResourceRootPath + "/who/csv/bfa_girls_p_exp.csv";
var hcfa_boysPath = moduleResourceRootPath + "/who/csv/hcfa_boys_p_exp.csv";
var hcfa_girlsPath = moduleResourceRootPath + "/who/csv/hcfa_girls_p_exp.csv";
var lhfa_boysPath = moduleResourceRootPath + "/who/csv/lhfa_boys_p_exp.csv";
var lhfa_girlsPath = moduleResourceRootPath + "/who/csv/lhfa_girls_p_exp.csv";
var wfa_boysPath = moduleResourceRootPath + "/who/csv/wfa_boys_p_exp.csv";
var wfa_girlsPath = moduleResourceRootPath + "/who/csv/wfa_girls_p_exp.csv";
var wfl_boysPath = moduleResourceRootPath + "/who/csv/wfl_boys_p_exp.csv";
var wfl_girlsPath = moduleResourceRootPath + "/who/csv/wfl_girls_p_exp.csv";
var growthChartCurveColors = {
	"P3" : "#009384",
	"P5" : "#5B57A6",
	"P10" : "#363463",
	"P15" : "#363463",
	"P25" : "#231F20",
	"P50" : "#007fff",
	"P75" : "#a1d030",
	"P90" : "#EEA616",
	"P95" : "cyan",
	"P97" : "#51a351",
	"Patient" : "#F26522",
	"Pub3" : "#808080",
	"Pub5" : "#808080",
	"Pub10" : "#808080",
	"Pub25" : "#808080",
	"Pub50" : "#808080",
	"Pub75" : "#808080",
	"Pub90" : "#808080",
	"Pub95" : "#808080",
	"Pub97" : "#808080",
	"Diff3" : "#808080",
	"Diff5" : "#808080",
	"Diff10" : "#808080",
	"Diff25" : "#808080",
	"Diff25" : "#808080",
	"Diff75" : "#808080",
	"Diff90" : "#808080",
	"Diff95" : "#808080",
	"Diff97" : "#808080",
	"P85" : "#4B0082"
};

function loadCSVIntoJson(csv, holizontalScaler) {
	var lines = csv.split("\n");
	var result = [];
	var headers = lines[0].replace("\r", "").split(",");

	if (csv !== undefined) {
		for (var i = 1; i < lines.length; i++) {
			var obj = {};
			var currentline = lines[i].replace("\r", "").split(",");

			for (var j = 0; j < headers.length; j++) {
				obj[headers[j]] = currentline[j];
			}

			if (obj[holizontalScaler] !== undefined)
				result.push(obj);
		}
		return JSON.stringify(result);
	}
}

function getFileContentFromServer(filePath) {
	var fetchedContent;

	jQuery.ajax({
		type : "GET",
		async : false,
		url : filePath,
		success : function(data) {
			fetchedContent = data;
		}
	});
	return fetchedContent;
}

function renderPatient_ChartJS_LineGraph_WeightAgeInf(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(wtageinfPath), "Agemos");

	drawInfantWeightOrHeightForAgeChartJSGraph(fetchedGrowthChartData,
			patientPropts, elementId, axisLabelNames, patientChartData);
}

function render_ChartJS_weightForAgeTwoToTwentyYears(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(wtagePath), "Agemos");

	drawTwoToTwentyWeightOrHeightForAgeChartJSGraph(patientPropts,
			fetchedGrowthChartData, elementId, axisLabelNames, patientChartData);
}

function render_ChartJS_statureForAgeTwoToTwentyYears(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(statagePath), "Agemos");

	drawTwoToTwentyWeightOrHeightForAgeChartJSGraph(patientPropts,
			fetchedGrowthChartData, elementId, axisLabelNames, patientChartData);
}

function drawTwoToTwentyWeightOrHeightForAgeChartJSGraph(patientPropts,
		fetchedGrowthChartData, elementId, axisLabelNames, patientChartData) {
	if (patientPropts.gender != undefined && patientPropts.age.years >= 2) {
		var data = setupCDCBasicGrowthChatMeta(fetchedGrowthChartData,
				patientPropts, "Agemos", true, patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderPatient_ChartJS_LineGraph_HeightAgeInf(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(hcageinfPath), "Agemos");

	drawInfantWeightOrHeightForAgeChartJSGraph(fetchedGrowthChartData,
			patientPropts, elementId, axisLabelNames, patientChartData);
}

function renderPatient_ChartJS_LineGraph_LengthAgeInf(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(lenageinfPath), "Agemos");

	drawInfantWeightOrHeightForAgeChartJSGraph(fetchedGrowthChartData,
			patientPropts, elementId, axisLabelNames, patientChartData);
}

function renderPatient_ChartJS_LineGraph_WeightLengthInf(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(wtleninfPath), "Length");

	if (patientPropts.gender != undefined) {
		var data = setupCDCBasicGrowthChatMeta(fetchedGrowthChartData,
				patientPropts, "Length", false, patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderPatient_ChartJS_LineGraph_WeightStature(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(wtstatPath), "Height");

	if (patientPropts.gender != undefined) {
		var data = setupCDCBasicGrowthChatMeta(fetchedGrowthChartData,
				patientPropts, "Height", false, patientChartData);
		var p85Values = [];
		var filtered_fetchedGrowthChartData = filterFetchedGrowthChartData(
				fetchedGrowthChartData, patientPropts, "Height", false);

		for (i = 0; i < filtered_fetchedGrowthChartData.length; i++) {
			p85Values.push(filtered_fetchedGrowthChartData[i].P85);
		}

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderPatient_ChartJS_LineGraph_bmiAge(patientChartData, elementId,
		patientPropts, axisLabelNames) {
	var fetchedGrowthChartData = loadCSVIntoJson(
			getFileContentFromServer(bmiAgeRevPath), "Agemos");

	if (patientPropts.gender != undefined && patientPropts.age.years >= 2) {
		var data = setupCDCBasicGrowthChatMeta(fetchedGrowthChartData,
				patientPropts, "Agemos", true, patientChartData);
		var p85Values = [];
		var filtered_fetchedGrowthChartData = filterFetchedGrowthChartData(
				fetchedGrowthChartData, patientPropts, "Agemos", true);

		for (i = 0; i < filtered_fetchedGrowthChartData.length; i++) {
			p85Values.push(filtered_fetchedGrowthChartData[i].P85);
		}

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function drawInfantWeightOrHeightForAgeChartJSGraph(fetchedGrowthChartData,
		patientPropts, elementId, axisLabelNames, patientChartData) {
	if (patientPropts.gender != undefined) {
		var data = setupCDCBasicGrowthChatMeta(fetchedGrowthChartData,
				patientPropts, "Agemos", false, patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function setupCDCBasicGrowthChatMeta(fetchedGrowthChartData, patientPropts,
		holizontalScaler, labelsOrHolizontalIsInYears, patientChartData) {
	var labels = [];
	var p3Values = [];
	var p5Values = [];
	var p10Values = [];
	var p25Values = [];
	var p50Values = [];
	var p75Values = [];
	var p90Values = [];
	var p95Values = [];
	var p97Values = [];
	var patientValues = [];
	var filtered_fetchedGrowthChartData = filterFetchedGrowthChartData(
			fetchedGrowthChartData, patientPropts, holizontalScaler);

	for (i = 0; i < filtered_fetchedGrowthChartData.length; i++) {
		var label = labelsOrHolizontalIsInYears == true ? Math
				.round(filtered_fetchedGrowthChartData[i][holizontalScaler] / 12)
				: Math
						.round(filtered_fetchedGrowthChartData[i][holizontalScaler]);

		if (jQuery.inArray(label, labels) < 0) {
			var patientValue = matchAndGetPatientValueForLabel(
					patientChartData, label);

			labels.push(label);
			p3Values.push(filtered_fetchedGrowthChartData[i].P3);
			p5Values.push(filtered_fetchedGrowthChartData[i].P5);
			p10Values.push(filtered_fetchedGrowthChartData[i].P10);
			p25Values.push(filtered_fetchedGrowthChartData[i].P25);
			p50Values.push(filtered_fetchedGrowthChartData[i].P50);
			p75Values.push(filtered_fetchedGrowthChartData[i].P75);
			p90Values.push(filtered_fetchedGrowthChartData[i].P90);
			p95Values.push(filtered_fetchedGrowthChartData[i].P95);
			p97Values.push(filtered_fetchedGrowthChartData[i].P97);
			patientValues.push(patientValue != undefined ? patientValue : null);
		}
	}
	var datasets = [ generate_ChartJS_dataset("P5", p5Values),
			generate_ChartJS_dataset("P10", p10Values),
			generate_ChartJS_dataset("P25", p25Values),
			generate_ChartJS_dataset("P50", p50Values),
			generate_ChartJS_dataset("P75", p75Values),
			generate_ChartJS_dataset("P90", p90Values),
			generate_ChartJS_dataset("P95", p95Values),
			generate_ChartJS_dataset("Patient", patientValues) ];

	return {
		labels : labels,
		datasets : datasets
	};
}

function filterFetchedGrowthChartData(fetchedGrowthChartData, patientPropts,
		holizontalScaler, labelsOrHolizontalIsInYears) {
	var filtered_fetchedGrowthChartData = [];
	var chartYears = [];

	fetchedGrowthChartData = JSON.parse(fetchedGrowthChartData);

	for (i = 0; i < fetchedGrowthChartData.length; i++) {
		if (fetchedGrowthChartData[i].Sex == patientPropts.gender) {
			if (labelsOrHolizontalIsInYears == true) {
				var year = Math
						.round(fetchedGrowthChartData[i][holizontalScaler] / 12);

				if (jQuery.inArray(year, chartYears) < 0) {
					chartYears.push(year);
					filtered_fetchedGrowthChartData
							.push(fetchedGrowthChartData[i]);
				}
			} else {
				filtered_fetchedGrowthChartData.push(fetchedGrowthChartData[i]);
			}
		}
	}

	filtered_fetchedGrowthChartData.sort(function(a, b) {
		return a[holizontalScaler] - b[holizontalScaler];
	});

	return filtered_fetchedGrowthChartData;
}

function drawLineChartJSGraph(elementId, data, axisLabelNames) {
	var ctx = document.getElementById(elementId);
	var myChart = new Chart(ctx, {
		type : 'line',
		data : data,
		options : {
			scales : {
				xAxes : [ {
					scaleLabel : {
						display : true,
						labelString : $.parseHTML(axisLabelNames.x)[0].textContent
					}
				} ],
				yAxes : [ {
					ticks : {
						beginAtZero : false
					},
					scaleLabel : {
						display : true,
						labelString : $.parseHTML(axisLabelNames.y)[0].textContent
					}
				} ]
			}
		}
	});
}

function generate_ChartJS_dataset(labelHackCode, dataValues) {
	return {
		label : labelHackCode,
		data : dataValues,
		borderColor : growthChartCurveColors[labelHackCode],
		borderWidth : ((labelHackCode == "Patient") ? 2 : 1),
		spanGaps : true
	};
}

function resetChartJSCanvas(canvasElementId) {
	$('#' + canvasElementId).remove();
	$('#' + canvasElementId + "-container").append(
			'<canvas id="' + canvasElementId + '"><canvas>');
}

function indentifySelectedCdcLink(linkElementId, who) {
	if (who == undefined) {
		resetChartLinkFont("growthChartCDCLink");
	} else if (who == "who") {
		resetChartLinkFont("growthChartWhoLink");
	}
	jQuery("#" + linkElementId).css({
		color : "#363463",
		"font-weight" : "bold",
		"font-size" : "115%"
	});
}

function resetChartLinkFont(linkClassName) {
	jQuery("." + linkClassName).css({
		color : "",
		"font-weight" : "",
		"font-size" : ""
	});
}

function matchAndGetPatientValueForLabel(patientChartData, label) {
	var patientValue = undefined;

	if (patientChartData != undefined && patientChartData.length > 0) {
		for (j = 0; j < patientChartData.length; j++) {
			if (patientChartData[j][label] != undefined
					&& Object.keys(patientChartData[j]) == label) {
				patientValue = patientChartData[j][label];
				break;
			}
		}
	}
	return patientValue;
}

function setupWHOBasicGrowthChatMeta(fetchedGrowthChartData, holizontalScaler,
		patientChartData) {
	if (fetchedGrowthChartData != undefined) {
		var labels = [];
		var p3Values = [];
		var p15Values = [];
		var p50Values = [];
		var p85Values = [];
		var p97Values = [];
		var patientValues = [];

		fetchedGrowthChartData = JSON.parse(fetchedGrowthChartData);
		for (i = 0; i < fetchedGrowthChartData.length; i++) {
			var month = Math
					.round(fetchedGrowthChartData[i][holizontalScaler] / 30.4375);

			if (month >= 0 && month <= 24) {
				var label = holizontalScaler != "Length" ? month : Math
						.round(fetchedGrowthChartData[i][holizontalScaler]);

				if (jQuery.inArray(label, labels) < 0) {
					var patientValue = matchAndGetPatientValueForLabel(
							patientChartData, label);

					labels.push(label);
					p3Values.push(fetchedGrowthChartData[i].P3);
					p15Values.push(fetchedGrowthChartData[i].P15);
					p50Values.push(fetchedGrowthChartData[i].P50);
					p85Values.push(fetchedGrowthChartData[i].P85);
					p97Values.push(fetchedGrowthChartData[i].P97);
					patientValues.push(patientValue != undefined ? patientValue
							: null);
				}
			}
		}
		var datasets = [ generate_ChartJS_dataset("P3", p3Values),
				generate_ChartJS_dataset("P15", p15Values),
				generate_ChartJS_dataset("P50", p50Values),
				generate_ChartJS_dataset("P85", p85Values),
				generate_ChartJS_dataset("P97", p97Values),
				generate_ChartJS_dataset("Patient", patientValues) ];

		return {
			labels : labels,
			datasets : datasets
		};
	}
}

function renderWHOPatient_ChartJS_LineGraph_WeightForAge(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	if (patientPropts.gender != undefined) {
		var fetchedGrowthChartData = patientPropts.gender == 1 ? loadCSVIntoJson(
				getFileContentFromServer(wfa_girlsPath), "Age")
				: (patientPropts.gender == 2 ? loadCSVIntoJson(
						getFileContentFromServer(wfa_boysPath), "Age")
						: undefined);

		var data = setupWHOBasicGrowthChatMeta(fetchedGrowthChartData, "Age",
				patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderWHOPatient_ChartJS_LineGraph_WeightForLength(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	if (patientPropts.gender != undefined) {
		var fetchedGrowthChartData = patientPropts.gender == 1 ? loadCSVIntoJson(
				getFileContentFromServer(wfl_girlsPath), "Length")
				: (patientPropts.gender == 2 ? loadCSVIntoJson(
						getFileContentFromServer(wfl_boysPath), "Length")
						: undefined);

		var data = setupWHOBasicGrowthChatMeta(fetchedGrowthChartData,
				"Length", patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderWHOPatient_ChartJS_LineGraph_LengthForAge(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	if (patientPropts.gender != undefined) {
		var fetchedGrowthChartData = patientPropts.gender == 1 ? loadCSVIntoJson(
				getFileContentFromServer(lhfa_girlsPath), "Day")
				: (patientPropts.gender == 2 ? loadCSVIntoJson(
						getFileContentFromServer(lhfa_boysPath), "Day")
						: undefined);

		var data = setupWHOBasicGrowthChatMeta(fetchedGrowthChartData, "Day",
				patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderWHOPatient_ChartJS_LineGraph_HeadCircumferenceForAge(
		patientChartData, elementId, patientPropts, axisLabelNames) {
	if (patientPropts.gender != undefined) {
		var fetchedGrowthChartData = patientPropts.gender == 1 ? loadCSVIntoJson(
				getFileContentFromServer(hcfa_girlsPath), "Age")
				: (patientPropts.gender == 2 ? loadCSVIntoJson(
						getFileContentFromServer(hcfa_boysPath), "Age")
						: undefined);

		var data = setupWHOBasicGrowthChatMeta(fetchedGrowthChartData, "Age",
				patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

function renderWHOPatient_ChartJS_LineGraph_BMIForAge(patientChartData,
		elementId, patientPropts, axisLabelNames) {
	if (patientPropts.gender != undefined) {
		var fetchedGrowthChartData = patientPropts.gender == 1 ? loadCSVIntoJson(
				getFileContentFromServer(bfa_girlsPath), "Age")
				: (patientPropts.gender == 2 ? loadCSVIntoJson(
						getFileContentFromServer(bfa_boysPath), "Age")
						: undefined);

		var data = setupWHOBasicGrowthChatMeta(fetchedGrowthChartData, "Age", patientChartData);

		drawLineChartJSGraph(elementId, data, axisLabelNames);
	}
}

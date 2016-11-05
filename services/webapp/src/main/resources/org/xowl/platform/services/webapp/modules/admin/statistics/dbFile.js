// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var fileName = getParameterByName("file");
var timeline = [];
var dataAccesses = [];
var dataContention = [];
var dataTotalBlocks = [];
var dataDirtyBlocks = [];

var RATE = 1000; // refresh rate in ms
var MAX_SAMPLES_COUNT = 60; // maximum number of samples

function init() {
	doSetupPage(xowl, true, [
			{name: "Administration Module", uri: "/web/modules/admin/"},
			{name: "Statistics", uri: "/web/admin/statistics/"},
			{name: "DB File " + fileName}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
	});
	document.getElementById("file-name").value = fileName;
}

function retrieveSample() {
	xowl.getDatabasesStatistics(function (status, ct, content) {
		if (status == 200) {
			for (var i = 0; i != content.longTerm.files.length; i++) {
				if (content.longTerm.files[i].fileName === fileName) {
					window.setTimeout(retrieveSample, RATE);
					onSample(content.longTerm.files[i]);
					return;
				}
			}
			for (var i = 0; i != content.live.files.length; i++) {
				if (content.live.files[i].fileName === fileName) {
					window.setTimeout(retrieveSample, RATE);
					onSample(content.live.files[i]);
					return;
				}
			}
			for (var i = 0; i != content.service.files.length; i++) {
				if (content.service.files[i].fileName === fileName) {
					window.setTimeout(retrieveSample, RATE);
					onSample(content.service.files[i]);
					return;
				}
			}
		}
	});
}

function onSample(stats) {
	arrayPush(timeline, Date.now());
	arrayPush(dataAccesses, stats.accessesPerSecond);
	arrayPush(dataContention, stats.accessesContention);
	arrayPush(dataTotalBlocks, stats.loadedBlocks);
	arrayPush(dataDirtyBlocks, stats.dirtyBlocks);
	render();
}

function arrayPush(array, value) {
	if (array.length < MAX_SAMPLES_COUNT) {
		array.push(value);
	} else {
		array.splice(0, 1);
		array.push(value);
	}
}

function render() {
	var graphWidth = 300;
	var graphHeight = graphWidth * 2 / 3;
	var plotAccesses = renderLinePlot({
		dataX: {
			totalSpan: (MAX_SAMPLES_COUNT - 1) * RATE,
			series: timeline,
			title: "time"
		},
		dataY: {
			min: 0,
			series: [
				{data: dataAccesses, color: "blue"}
			],
			title: "accesses"
		}
	}, graphWidth, graphHeight);
	var display = document.getElementById("graph-accesses");
	while (display.hasChildNodes())
		display.removeChild(display.lastChild);
	display.appendChild(plotAccesses);

	var plotContention = renderLinePlot({
		dataX: {
			totalSpan: (MAX_SAMPLES_COUNT - 1) * RATE,
			series: timeline,
			title: "time"
		},
		dataY: {
			min: 0,
			series: [
				{data: dataContention, color: "blue"}
			],
			title: "tries"
		}
	}, graphWidth, graphHeight);
	display = document.getElementById("graph-contention");
	while (display.hasChildNodes())
		display.removeChild(display.lastChild);
	display.appendChild(plotContention);

	var plotBlocks = renderLinePlot({
		dataX: {
			totalSpan: (MAX_SAMPLES_COUNT - 1) * RATE,
			series: timeline,
			title: "time"
		},
		dataY: {
			min: 0,
			series: [
				{data: dataTotalBlocks, color: "blue"},
				{data: dataDirtyBlocks, color: "red"}
			],
			title: "blocks"
		}
	}, graphWidth, graphHeight);
	display = document.getElementById("graph-blocks");
	while (display.hasChildNodes())
		display.removeChild(display.lastChild);
	display.appendChild(plotBlocks);
}
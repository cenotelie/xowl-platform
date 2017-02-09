// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var metricId = getParameterByName("id");
var METRIC = null;
var SNAPSHOT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Statistics", uri: ROOT + "/modules/admin/statistics/"},
			{name: "Metric " + metricId}], function() {
		if (!metricId || metricId === null || metricId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getMetric(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			METRIC = content;
			document.getElementById("metric-name").value = METRIC.name;
			doRender();
		}
	}, metricId);
	xowl.getMetricSnapshot(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			SNAPSHOT = content;
			doRender();
		}
	}, metricId);
}

function doRender() {
	if (METRIC == null || SNAPSHOT == null)
		return;
	var table = document.getElementById("data");
	while (table.hasChildNodes()) {
		table.removeChild(table.lastChild);
	}
	renderCouple(table, METRIC, SNAPSHOT, 0);
}

function renderCouple(table, metric, value, offset) {
	if (metric.hints.isComposite === "true") {
		return renderCoupleCompositeMetric(table, metric, value, offset);
	} else {
		return renderCoupleSimpleMetric(table, metric, value, offset);
	}
}

function renderCoupleSimpleMetric(table, metric, value, offset) {
	var value = JSON.stringify(value.value) + " " + metric.unit;
	var span = document.createElement("span");
	span.style.marginLeft = offset + "cm";
	var cell1 = document.createElement("td");
	cell1.appendChild(span);
	cell1.appendChild(document.createTextNode(metric.name));
	var cell2 = document.createElement("td");
	cell2.appendChild(document.createTextNode(value));
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	table.appendChild(row);
	return metric.name;
}

function renderCoupleCompositeMetric(table, metric, value, offset) {
	renderCoupleCompositeMetricHeader(table, metric, offset);
	var map = new Map();
	for (var i = 0; i != metric.parts.length; i++) {
		map.set(metric.parts[i].identifier, metric.parts[i]);
	}
	var names = Object.getOwnPropertyNames(value.parts);
	for (var p = 0; p != names.length; p++) {
		var subValue = value.parts[names[p]];
		var subMetric = map.get(names[p]);
		renderCouple(table, subMetric, subValue, offset + 1);
	}
	return metric.name;
}

function renderCoupleCompositeMetricHeader(table, metric, offset) {
	var span = document.createElement("span");
	span.style.marginLeft = offset + "cm";
	var cell1 = document.createElement("td");
	cell1.appendChild(span);
	cell1.appendChild(document.createTextNode(metric.name));
	var cell2 = document.createElement("td");
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	table.appendChild(row);
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Administration Module", uri: "/web/modules/admin/"},
			{name: "Statistics"}], function() {
		var remover = displayLoader("Loading ...");
		xowl.getStatisticsList(function (status, ct, content) {
			if (status == 200) {
				renderMetrics(content);
				remover();
			} else {
				remover();
				displayMessageHttpError(status, content);
			}
		});
	});
}

function renderMetrics(metrics) {
	metrics.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("metrics");
	for (var  i = 0; i != metrics.length; i++) {
		table.appendChild(renderMetric(metrics[i]));
	}
}

function renderMetric(metric) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(metric.name));
	link.href="metric.html?id=" + encodeURIComponent(metric.identifier);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

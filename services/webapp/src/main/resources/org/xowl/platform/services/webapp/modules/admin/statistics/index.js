// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Statistics"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getAllMetrics(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderMetrics(content);
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

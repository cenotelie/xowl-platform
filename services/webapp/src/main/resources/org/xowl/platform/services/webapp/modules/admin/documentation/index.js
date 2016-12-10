// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Documentation"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getApiServices(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderServices(content);
			}
		});
	});
}

function renderServices(services) {
	services.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("services");
	for (var  i = 0; i != services.length; i++) {
		table.appendChild(renderService(services[i], i + 1));
	}
}

function renderService(service, number) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	cell.appendChild(document.createTextNode(number));
	row.appendChild(cell);

	cell = document.createElement("td");
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(service.name));
	if ((typeof service.documentation.fileName) !== "undefined")
		link.href="/web/contributions/documentation/" + encodeURIComponent(service.documentation.fileName);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	if ((typeof service.specification.fileName) !== "undefined") {
		link = document.createElement("a");
		link.appendChild(document.createTextNode(service.specification.fileName));
		link.href="/web/contributions/documentation/" + encodeURIComponent(service.specification.fileName);
		cell.appendChild(link);
	}
	row.appendChild(cell);
	return row;
}

// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var connectorId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Connectors Management", uri: ROOT + "/modules/admin/connectors/"},
			{name: "Connector " + connectorId}], function() {
		if (!connectorId || connectorId === null || connectorId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getConnector(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				render(content);
			}
		}, connectorId);
	});
}

function render(connector) {
	var url = null;
	if (connector.uris.length === 0) {
		url = "not accessible";
	} else {
		url = xowl.endpoint + connector.uris[0];
	}

	document.getElementById("connector-identifier").value = connector.identifier;
	document.getElementById("connector-name").value = connector.name;
	document.getElementById("connector-uri").value = url;
	document.getElementById("connector-can-pull").value = (connector.canPullInput ? "YES" : "NO");
	var rows = document.getElementById("connector-queue");
	var data = "";
	for (var i = 0; i != connector.queue.length; i++) {
		data += "<tr><td>" + i.toString() + "</td><td> <img src=\"" + ROOT + "/assets/artifact.svg\" width=\"40\" height=\"40\" />" + connector.queue[i].name + "</td><td>" + connector.queue[i].version + "</td></tr>";
	}
	rows.innerHTML = data;
}

function onClickPull() {
	var result = confirm("Pull from connector " + document.getElementById("connector-name").value + "?");
	if (!result)
		return;
	if (!onOperationRequest("Launching a pull artifact operation from " + document.getElementById("connector-name").value + " ..."))
		return;
	xowl.pullFromConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched job ", content]});
			waitForJob(content.identifier, content.name, function (job) {
				onPullJobComplete(job.result);
			});
		}
	}, connectorId);
}

function onPullJobComplete(xsp) {
	if (!xsp.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!xsp.isSuccess) {
		displayMessage("error", "FAILURE: " + xsp.message);
	} else {
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: [
			"Pulled artifact ",
			{type: "org.xowl.platform.kernel.artifacts.Artifact", identifier: xsp.payload, name: xsp.payload}]});
	}
}
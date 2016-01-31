// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var connectorId = getParameterByName("id");
var JOB = null;

function init() {
	setupPage(xowl);
	if (!connectorId || connectorId === null || connectorId === "")
		return;
	document.getElementById("placeholder-connector").innerHTML = connectorId;
	displayMessage("Loading ...");
	xowl.getConnector(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, connectorId);
}

function render(connector) {
	var url = null;
	if (connector.uris.length === 0) {
		url = "not accessible";
	} else {
		url = document.URL;
		var index = url.indexOf("/web/");
		if (index > 0)
			url = url.substring(0, index) + "/api/" + connector.uris[0];
		else
			url = "failed to retrieved"
	}

	document.getElementById("connector-identifier").value = connector.identifier;
	document.getElementById("connector-name").value = connector.name;
	document.getElementById("connector-uri").value = url;
	document.getElementById("connector-can-pull").value = (connector.canPullInput ? "YES" : "NO");
	var rows = document.getElementById("connector-queue");
	var data = "";
	for (var i = 0; i != connector.queue.length; i++) {
		data += "<tr><td>" + i.toString() + "</td><td> <img src=\"/web/assets/artifact.svg\" width=\"40\" height=\"40\" />" + connector.queue[i].name + "</td><td>" + connector.queue[i].version + "</td></tr>";
	}
	rows.innerHTML = data;
}

function onClickPull() {
	if (JOB !== null) {
		alert("Please wait for the previous action to terminate.");
		return;
	}
	JOB = "reserved";
	xowl.pullFromConnector(function (status, ct, content) {
		if (status == 200) {
			trackJob(content.identifier, "Working ...", function (isSuccess) {
				if (isSuccess)
					window.location.reload(true);
			});
		} else {
			displayMessage(getErrorFor(status, content));
			JOB = null;
		}
	}, connectorId);
}
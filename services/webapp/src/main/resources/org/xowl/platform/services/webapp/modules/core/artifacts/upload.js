// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var CONNECTORS = null;

function init() {
	setupPage(xowl);
	document.getElementById("panel-next").style.display = "none";
	xowl.getConnectors(function (status, ct, content) {
		if (status == 200) {
			CONNECTORS = content;
			renderConnectors();
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderConnectors() {
	var select = document.getElementById("input-connector");
	for (var i = 0; i != CONNECTORS.length; i++) {
		var option = document.createElement("option");
		option.value = CONNECTORS[i].identifier;
		option.appendChild(document.createTextNode(CONNECTORS[i].name));
		select.appendChild(option);
	}
	if (CONNECTORS.length > 0) {
		select.value = CONNECTORS[0].identifier;
		document.getElementById("panel-next").style.display = "";
		onConnectorSelected();
	}
	displayMessage(null);
}

function onConnectorSelected() {
	var id = document.getElementById("input-connector").value;
	document.getElementById("btn-new").href = "upload-new.html?connector=" + encodeURIComponent(id);
	document.getElementById("btn-update").href = "upload-update.html?connector=" + encodeURIComponent(id);
}
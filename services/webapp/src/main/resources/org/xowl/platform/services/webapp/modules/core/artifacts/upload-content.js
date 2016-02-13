// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var CONNECTOR = getParameterByName("connector");
var CONNECTOR_URI = null;
var ARTIFACT_TYPE = getParameterByName("what");
var ARTIFACT_NAME = getParameterByName("name");
var ARTIFACT_BASE = getParameterByName("base");
var ARTIFACT_VERSION = getParameterByName("version");
var ARTIFACT_SUPERSEDED = getParameterByName("superseded");
var ARTIFACT_ARCHETYPE = getParameterByName("archetype");

function init() {
	setupPage(xowl);
	if (!CONNECTOR || CONNECTOR === null || CONNECTOR === ""
		|| !ARTIFACT_TYPE || ARTIFACT_TYPE === null || ARTIFACT_TYPE === ""
		|| !ARTIFACT_NAME || ARTIFACT_NAME === null || ARTIFACT_NAME === ""
		|| !ARTIFACT_BASE || ARTIFACT_BASE === null || ARTIFACT_BASE === ""
		|| !ARTIFACT_VERSION || ARTIFACT_VERSION === null || ARTIFACT_VERSION === ""
		|| !ARTIFACT_SUPERSEDED || ARTIFACT_SUPERSEDED === null || ARTIFACT_SUPERSEDED === ""
		|| !ARTIFACT_ARCHETYPE || ARTIFACT_ARCHETYPE === null || ARTIFACT_ARCHETYPE === "")
		document.location.href = "upload.html";
	document.getElementById("input-connector").value = CONNECTOR;
	document.getElementById("input-artifact-name").value = ARTIFACT_NAME;
	document.getElementById("input-artifact-base").value = ARTIFACT_BASE;
	document.getElementById("input-artifact-version").value = ARTIFACT_VERSION;
	document.getElementById("input-artifact-superseded").value = ARTIFACT_SUPERSEDED;
	document.getElementById("input-artifact-archetype").value = ARTIFACT_ARCHETYPE;
	var typesField = document.getElementById("input-syntax");
	for (var i = 0; i != MIME_TYPES.length; i++) {
		var option = document.createElement("option");
		option.value = MIME_TYPES[i].value;
		option.appendChild(document.createTextNode(MIME_TYPES[i].name));
		typesField.appendChild(option);
	}
	xowl.getConnector(function (code, type, content) {
		if (code === 200) {
			if (content.uris.length <= 0) {
				displayMessage("Error: The connector does not specify an access URI.");
			} else {
				CONNECTOR_URI = content.uris[0];
				displayMessage(null);
			}
		} else {
			displayMessage(getErrorFor(code, content));
		}
	}, CONNECTOR);
}

function onImport() {
	var content = document.getElementById("input-content").value;
	if (!content || content === null || content === "")
		return;
	var selectedMIME = document.getElementById("input-syntax").value;
	var progressBar = document.getElementById("import-progress");
	progressBar['aria-valuenow'] = 0;
	progressBar.style.width = "0%";
	progressBar.classList.remove("progress-bar-success");
	progressBar.classList.remove("progress-bar-error");
	progressBar.innerHTML = null;
	displayMessage("Sending ...");
	xowl.upload(function (code, type, content) {
		if (code === 200) {
			progressBar.classList.add("progress-bar-success");
			displayMessage(null);
			alert("OK");
			window.location.href = "/web/modules/core/artifacts/";
		} else {
			displayMessage(getErrorFor(code, content));
			progressBar.classList.add("progress-bar-error");
		}
		progressBar['aria-valuenow'] = 100;
		progressBar.style.width = "100%";
	}, CONNECTOR_URI, content, selectedMIME, ARTIFACT_NAME, ARTIFACT_BASE, ARTIFACT_VERSION, ARTIFACT_SUPERSEDED, ARTIFACT_ARCHETYPE);
}
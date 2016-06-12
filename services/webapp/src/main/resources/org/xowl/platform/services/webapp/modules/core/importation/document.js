// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var lastPreview = null;
var mapping = [];

function init() {
	setupPage(xowl);
	if (!docId || docId === null || docId === "")
    	return;
	document.getElementById("placeholder-doc").innerHTML = docId;
	displayMessage("Loading ...");
	xowl.getUploadedDocument(function (status, ct, content) {
		if (status == 200) {
			document.getElementById("document-name").value = content.name;
			xowl.getDocumentImporters(function (status, ct, content) {
				if (status == 200) {
					renderImporters(content);
					displayMessage(null);
				} else {
					displayMessage(getErrorFor(status, content));
				}
			}, docId);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId);
}

function renderImporters(importers) {
	var select = document.getElementById("importers");
	for (var i = 0; i != importers.length; i++) {
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(importers[i].name));
		option.value = importers[i].wizardUri;
		select.appendChild(option);
	}
}

function onImport() {
	var wizard = document.getElementById("importers").value;
	window.location.href = wizard + "?id=" + encodeURIComponent(docId);
}

function onDrop() {
	displayMessage("Dropping the document ...");
	xowl.dropUploadedDocument(function (status, ct, content) {
		if (status == 200) {
			window.location.href = "index.html";
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId);
}
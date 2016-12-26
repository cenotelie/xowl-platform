// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var DOCUMENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Document " + docId}], function() {
		if (!docId || docId === null || docId === "")
    		return;
    	doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			DOCUMENT = content;
			document.getElementById("document-name").value = DOCUMENT.name;
			document.getElementById("document-upload-date").value = DOCUMENT.uploadDate;
			document.getElementById("document-uploader").value = DOCUMENT.uploader;
			document.getElementById("document-file-name").value = DOCUMENT.fileName;
		}
	}, docId);
	xowl.getDocumentImporters(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderImporters(content);
		}
	});
}

function renderImporters(importers) {
	var select = document.getElementById("importers");
	for (var i = 0; i != importers.length; i++) {
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(importers[i].name));
		option.value = importers[i].identifier;
		select.appendChild(option);
	}
}

function onImportNew() {
	var importerId = document.getElementById("importers").value;
	window.location.href = "document-new.html?id=" + encodeURIComponent(docId) + "&importer=" + encodeURIComponent(importerId);
}

function onImportUpdate() {
	var importerId = document.getElementById("importers").value;
	window.location.href = "document-update.html?id=" + encodeURIComponent(docId) + "&importer=" + encodeURIComponent(importerId);
}

function onDrop() {
	var result = confirm("Drop document " + DOCUMENT.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Dropping document ", DOCUMENT, " ..."]}))
		return;
	xowl.dropUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Dropped document ", DOCUMENT, "."]});
			waitAndGo("index.html");
		}
	}, docId);
}
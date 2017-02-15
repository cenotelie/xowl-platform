// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var doc = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Document " + docId}], function() {
		if (!docId || docId === null || docId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			doc = content;
			document.getElementById("document-id").value = doc.identifier;
			document.getElementById("document-name").value = doc.name;
			document.getElementById("document-upload-date").value = doc.uploadDate;
			document.getElementById("document-uploader").value = doc.uploader;
			document.getElementById("document-file-name").value = doc.fileName;
		}
	}, docId);
}

function onImportNew() {
	window.location.href = "document-import.html?id=" + encodeURIComponent(docId) + "&new=true";
}

function onImportUpdate() {
	window.location.href = "document-import.html?id=" + encodeURIComponent(docId) + "&new=false";
}

function onDrop() {
	var result = confirm("Drop document " + doc.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Dropping document ", doc, " ..."]}))
		return;
	xowl.dropUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Dropped document ", doc, "."]});
			waitAndGo("index.html");
		}
	}, docId);
}
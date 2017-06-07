// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var doc = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Import Data", uri: ROOT + "/modules/core/importation/"},
			{name: "Document " + docId}], function() {
		if (!docId || docId === null || docId === "")
			return;
		doGetData();
		setupSRAutocomplete();
		RESOURCE = docId;
	}, ["secured-resource-popups", "secured-resource-descriptor"]);
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
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
	xowl.getSecuredResourceDescriptor(function (status, ct, content) {
		onOperationEnded(200, content);
		if (status === 200) {
			renderDescriptor(content);
		}
	}, docId);
}

function onClickImport() {
	window.location.href = "document-import.html?id=" + encodeURIComponent(doc.identifier);
}

function onClickDelete() {
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
	}, doc.identifier);
}
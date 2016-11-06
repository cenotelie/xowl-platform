// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var importerId = getParameterByName("importer");
var importerWizard = null;
var DOCUMENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Document ", uri: "document.html?id=" + encodeURIComponent(docId)},
			{name: "Import as New"}], function() {
		if (!docId || docId === null || docId === "")
    		return;
		doGetDocument();
	});
}

function doGetDocument() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			DOCUMENT = content;
			document.getElementById("document-name").value = DOCUMENT.name;
		}
		doGetImporter();
	}, docId);
}

function doGetImporter() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getDocumentImporter(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			document.getElementById("importer").value = content.name;
			importerWizard = content.wizardUri;
		}
		doGetArchetypes();
	}, importerId);
}

function doGetArchetypes() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderArchetypes(content);
		}
	});
}

function renderArchetypes(data) {
	var select = document.getElementById("input-archetype");
	for (var i = 0; i != data.length; i++) {
		var option = document.createElement("option");
		option.value = data[i].id;
		option.appendChild(document.createTextNode(data[i].name));
		select.appendChild(option);
	}
	if (data.length > 0)
		select.value = data[0].id;
}

function onImport() {
	var base = document.getElementById("input-base").value;
	var version = document.getElementById("input-init-version").value;
	var archetype = document.getElementById("input-archetype").value;
	if (base === null || base === "" || version === null || version === "" || archetype === null || archetype === "")
		return;
	window.location.href = importerWizard +
		"?id=" + encodeURIComponent(docId) +
		"&base=" + encodeURIComponent(base) +
		"&version=" + encodeURIComponent(version) +
		"&archetype=" + encodeURIComponent(archetype);
}
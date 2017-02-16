// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var storageId = getParameterByName("storageId");
var importerId = localStorage.getItem(storageId + ".importer.identifier");
var doc = {
	type: "org.xowl.platform.services.importation.Document",
	identifier: localStorage.getItem(storageId + ".document.identifier"),
	name: localStorage.getItem(storageId + ".document.name")
};
var metadata = {
	name: localStorage.getItem(storageId + ".artifact.name"),
	base: localStorage.getItem(storageId + ".artifact.base"),
	version: localStorage.getItem(storageId + ".artifact.version"),
	archetype: localStorage.getItem(storageId + ".artifact.archetype"),
	superseded: localStorage.getItem(storageId + ".artifact.superseded")
};

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Document " + doc.identifier, uri: ROOT + "/modules/core/importation/document.html?id=" + encodeURIComponent(doc.identifier)},
			{name: "Semantic Web Importer"}], function() {
		if (!storageId || storageId === null || storageId === "")
			return;
		var typesField = document.getElementById("input-syntax");
		for (var i = 0; i != MIME_TYPES.length; i++) {
			var option = document.createElement("option");
			option.value = MIME_TYPES[i].value;
			option.appendChild(document.createTextNode(MIME_TYPES[i].name));
			typesField.appendChild(option);
		}
		document.getElementById("document-id").value = doc.identifier;
		document.getElementById("document-name").value = doc.name;
	});
}

function onClickOk() {
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Importing document ", doc, " ..."]}))
		return;
	xowl.importUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched importation job for ", doc, "."]});
			waitForJob(content.identifier, content.name, function (job) {
				onJobCompleted(job);
			});
		}
	}, doc.identifier, {
		type: "org.xowl.platform.connectors.semanticweb.SemanticWebImporterConfiguration",
		identifier: "anonymous",
		name: "Anonymous Configuration",
		importer: importerId,
		syntax: document.getElementById("input-syntax").value
	}, metadata);
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		var artifactId = job.result.payload;
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Imported ", doc, " as artifact " + artifactId]});
		waitAndGo(ROOT + "/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(artifactId));
	}
}
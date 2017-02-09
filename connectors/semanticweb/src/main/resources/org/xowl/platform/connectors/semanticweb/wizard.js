// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var base = getParameterByName("base");
var version = getParameterByName("version");
var archetype = getParameterByName("archetype");
var importerId = "org.xowl.platform.connectors.semanticweb.SemanticWebImporter";
var DOCUMENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Document ", uri: "document.html?id=" + encodeURIComponent(docId)},
			{name: "Semantic Web Importer"}], function() {
		if (!docId || docId === null || docId === "")
			return;
		var typesField = document.getElementById("input-syntax");
		for (var i = 0; i != MIME_TYPES.length; i++) {
			var option = document.createElement("option");
			option.value = MIME_TYPES[i].value;
			option.appendChild(document.createTextNode(MIME_TYPES[i].name));
			typesField.appendChild(option);
		}
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
			var fileType = null;
			for (var i = 0; i != MIME_TYPES.length; i++) {
				for (var j = 0; j != MIME_TYPES[i].extensions.length; j++) {
					var suffix = MIME_TYPES[i].extensions[j];
					if (DOCUMENT.fileName.indexOf(suffix, DOCUMENT.fileName.length - suffix.length) !== -1) {
						fileType = MIME_TYPES[i];
						break;
					}
				}
			}
			if (fileType !== null) {
				document.getElementById("input-syntax").value = fileType.value;
			}
		}
	}, docId);
}

function onImport() {
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Importing document ", DOCUMENT, " ..."]}))
		return;
	xowl.importUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched importation job for ", DOCUMENT, "."]});
			waitForJob(content.identifier, content.name, function (job) {
				onJobCompleted(job);
			});
		}
	}, docId, importerId, {
		family: base,
		version: version,
		archetype: archetype,
		superseded: [],
		syntax: document.getElementById("input-syntax").value
	});
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		var artifactId = job.result.payload;
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Imported ", DOCUMENT, " as artifact " + artifactId]});
		waitAndGo(ROOT + "/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(artifactId));
	}
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var base = getParameterByName("base");
var version = getParameterByName("version");
var archetype = getParameterByName("archetype");
var importerId = "org.xowl.platform.connectors.doors9.DOORS9Importer";
var DOCUMENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Document ", uri: "document.html?id=" + encodeURIComponent(docId)},
			{name: "DOORS 9 Importer"}], function() {
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
	}, docId);
}

function onImport() {
	if (!onOperationRequest({ type: "org.xowl.platform.kernel.RichString", parts: ["Importing document ", DOCUMENT, " ..."]}))
		return;
	xowl.importUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.platform.kernel.RichString", parts: ["Launched importation job for ", DOCUMENT, "."]});
			waitForJob(content.identifier, content.name, function (job) {
				onJobCompleted(job);
			});
		}
	}, docId, importerId, {
	    family: base,
	    version: version,
	    archetype: archetype,
	    superseded: []
	});
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		var artifactId = job.result.payload;
		displayMessage("success", { type: "org.xowl.platform.kernel.RichString", parts: ["Imported ", DOCUMENT, " as artifact " + artifactId]});
		waitAndGo("/web/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(artifactId));
	}
}
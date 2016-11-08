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
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Document ", uri: "document.html?id=" + encodeURIComponent(docId)},
			{name: "Semantic Web Importer"}], function() {
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
		}
	}, docId);
}

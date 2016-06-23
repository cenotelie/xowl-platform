// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var base = getParameterByName("base");
var version = getParameterByName("version");
var archetype = getParameterByName("archetype");
var importerId = "org.xowl.platform.connectors.doors9.DOORS9Importer"
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
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId);
}


function onImport() {
	xowl.importUploadedDocument(function (status, ct, content) {
		if (status == 200) {

			displayMessage("Yeaahhhh");
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId, importerId, {
	    family: base,
	    version: version,
	    archetype: archetype,
	    superseded: []
	});
}
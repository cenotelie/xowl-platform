// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var importerId = getParameterByName("importer");
var importerWizard = null;

function init() {
	setupPage(xowl);
	if (!docId || docId === null || docId === "")
    	return;
	document.getElementById("placeholder-doc").innerHTML = docId;
	displayMessage("Loading ...");
	var loader = new Loader(3);
	xowl.getUploadedDocument(function (status, ct, content) {
		if (status == 200) {
			document.getElementById("document-name").value = content.name;
			loader.onLoaded();
		} else {
			loader.onError(status, content);
		}
	}, docId);
	xowl.getDocumentImporter(function (status, ct, content) {
		if (status == 200) {
			document.getElementById("importer").value = content.name;
			importerWizard = content.wizardUri;
			loader.onLoaded();
		} else {
			loader.onError(status, content);
		}
	}, importerId);
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (status == 200) {
			renderArchetypes(content);
			loader.onLoaded();
		} else {
			loader.onError(status, content);
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
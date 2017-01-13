// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var importerId = getParameterByName("importer");
var importerWizard = null;
var ARTIFACTS = {};
var DOCUMENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Document ", uri: "document.html?id=" + encodeURIComponent(docId)},
			{name: "Import as Update"}], function() {
		if (!docId || docId === null || docId === "")
    		return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 4))
		return;
	xowl.getUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			DOCUMENT = content;
			document.getElementById("document-name").value = DOCUMENT.name;
		}
	}, docId);
	xowl.getDocumentImporter(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			document.getElementById("importer").value = content.name;
			importerWizard = content.wizardUri;
		}
	}, importerId);
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderArchetypes(content);
		}
	});
	xowl.getAllArtifacts(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderFamilies(content);
		}
	});
}

function renderArchetypes(data) {
	var select = document.getElementById("input-archetype");
	for (var i = 0; i != data.length; i++) {
		var option = document.createElement("option");
		option.value = data[i].identifier;
		option.appendChild(document.createTextNode(data[i].name));
		select.appendChild(option);
	}
	if (data.length > 0)
		select.value = data[0].identifier;
}

function renderFamilies(data) {
	for (var i = 0; i != data.length; i++) {
		var artifact = data[i];
		if (artifact.hasOwnProperty("base") && artifact.base != "") {
			if (ARTIFACTS.hasOwnProperty(artifact.base)) {
				ARTIFACTS[artifact.base].push(artifact);
			} else {
				ARTIFACTS[artifact.base] = [artifact];
			}
		}
	}
	var select = document.getElementById("input-family");
	var names = Object.getOwnPropertyNames(ARTIFACTS);
	for (var i = 0; i != names.length; i++) {
		var elements = ARTIFACTS[names[i]];
		elements.sort(compareArtifacts);
		var option = document.createElement("option");
		option.value = elements[0].base;
		option.appendChild(document.createTextNode(elements[0].name));
		select.appendChild(option);
	}
	if (names.length > 0) {
		select.value = ARTIFACTS[names[0]][0].base;
		renderArtifacts(ARTIFACTS[names[0]]);
	}
}

function renderArtifacts(artifacts) {
	var base = document.getElementById("input-family").value;
	if (base === null || base === "")
		return;
	var select = document.getElementById("input-superseded");
	while (select.hasChildNodes())
		select.removeChild(select.lastChild);
	var option = document.createElement("option");
	option.value = "none";
	option.appendChild(document.createTextNode("None"));
	select.appendChild(option);
	for (var i = 0; i != ARTIFACTS[base].length; i++) {
		var artifact = ARTIFACTS[base][i];
		option = document.createElement("option");
		option.value = artifact.identifier;
		option.appendChild(document.createTextNode(artifact.name + " (" + artifact.version + ")"));
		select.appendChild(option);
	}
	select.value = "none";
}

function onFamilyChange() {
	var base = document.getElementById("input-family").value;
	if (base !== null || base !== "")
		renderArtifacts(ARTIFACTS[base]);
}

function onImport() {
	var base = document.getElementById("input-family").value;
	var superseded = document.getElementById("input-superseded").value;
	var version = document.getElementById("input-new-version").value;
	var archetype = document.getElementById("input-archetype").value;
	if (base === null || base === "" || superseded === null || superseded === "" || version === null || version === "" || archetype === null || archetype === "")
		return;
	window.location.href = importerWizard +
		"?id=" + encodeURIComponent(docId) +
		"&base=" + encodeURIComponent(base) +
		"&superseded=" + encodeURIComponent(superseded) +
		"&version=" + encodeURIComponent(version) +
		"&archetype=" + encodeURIComponent(archetype);
}
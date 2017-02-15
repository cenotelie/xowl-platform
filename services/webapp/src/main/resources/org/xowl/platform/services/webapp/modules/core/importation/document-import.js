// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var doc = null;
var importers = null;
var archetypes = null;
var artifacts = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Document " + docId, uri: ROOT + "/modules/core/importation/document.html?id=" + encodeURIComponent(docId)},
			{name: "Import"}], function() {
		if (!docId || docId === null || docId === "")
			return;
		setupAutocompleteArtifacts("input-superseded");
		setupAutocompleteArchetype("input-artifact-archetype");
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 3))
		return;
	xowl.getUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			doc = content;
			document.getElementById("document-id").value = doc.identifier;
			document.getElementById("document-name").value = doc.name;
			document.getElementById("input-artifact-name").value = doc.name;
		}
	}, docId);
	xowl.getDocumentImporters(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			importers = content;
			renderImporters(content);
		}
	});
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			archetypes = content;
		}
	});
}

function filterItems(items, value) {
	var result = [];
	for (var i = 0; i != items.length; i++) {
		if (items[i].identifier.indexOf(value) >= 0 || items[i].name.indexOf(value) >= 0) {
			result.push(items[i]);
		}
	}
	return result;
}

function setupAutocompleteArtifacts(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		if (artifacts !== null) {
			autocomplete.onItems(filterItems(artifacts, value));
			return;
		}
		xowl.getAllArtifacts(function (status, ct, content) {
			if (status === 200) {
				artifacts = content;
				autocomplete.onItems(filterItems(artifacts, value));
			}
		});
	};
	autocomplete.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " - " + item.version + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete.getItemString = function (item) {
		return item.identifier;
	};
	autocomplete.onItemSelected = function(item) {
		document.getElementById("input-artifact-name").value = item.name;
		document.getElementById("input-artifact-base").value = item.base;
		document.getElementById("input-artifact-archetype").value = item.archetype;
	};
}

function setupAutocompleteArchetype(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		autocomplete.onItems(filterItems(archetypes, value));
	};
	autocomplete.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete.getItemString = function (item) {
		return item.identifier;
	};
}

function renderImporters(importers) {
	importers.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("input-importer");
	for (var i = 0; i != importers.length; i++) {
		var option = document.createElement("option");
		var icon = document.createElement("img");
		icon.src = ROOT + "/assets/importer.svg";
		icon.width = 40;
		icon.height = 40;
		icon.style.marginRight = "20px";
		icon.title = importers[i].identifier;
		option.appendChild(icon);
		option.appendChild(document.createTextNode(importers[i].name));
		option.value = importers[i].identifier;
		select.appendChild(option);
	}
}

function onClickNext() {
}
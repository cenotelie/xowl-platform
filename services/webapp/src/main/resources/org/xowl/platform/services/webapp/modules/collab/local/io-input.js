// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var specId = getParameterByName("id");
var archetypes = null;
var input = null;
var artifacts = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "Inputs & Outputs", uri: "/web/modules/collab/local/io.html"},
			{name: "Input " + specId}], function() {
		if (!specId || specId === null || specId === "")
			return;
		setupAutocomplete();
		doGetData();
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("input-artifact");
	autocomplete1.lookupItems = function (value) {
		if (artifacts !== null) {
			autocomplete1.onItems(filterItems(artifacts, value));
			return;
		}
		xowl.getArtifactsForArchetype(function (status, ct, content) {
			if (status === 200) {
				artifacts = content;
				autocomplete1.onItems(filterItems(artifacts, value));
			}
		}, input.specification.archetype);
	};
	autocomplete1.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " - " + item.version + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete1.getItemString = function (item) {
		return item.identifier;
	};
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

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			for (var i = 0; i != content.inputs.length; i++) {
				if (content.inputs[i].specification.identifier == specId) {
					input = content.inputs[i];
					break;
				}
			}
			if (input !== null && archetypes !== null) {
				renderSpecification();
				doGetArtifacts();
			}
		}
	});
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			archetypes = content;
			if (input !== null) {
				renderSpecification();
				doGetArtifacts();
			}
		}
	});
}

function doGetArtifacts() {
	if (input.artifacts.length <= 0)
		return;
	if (!onOperationRequest("Loading ...", input.artifacts.length))
		return;
	for (var i = 0; i != input.artifacts.length; i++) {
		xowl.getArtifact(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderArtifact(content);
			}
		}, input.artifacts[i]);
	}
}

function renderSpecification() {
	document.getElementById("spec-identifier").value = input.specification.identifier;
	document.getElementById("spec-name").value = input.specification.name;
	document.getElementById("spec-archetype").appendChild(renderArchetype(input.specification.archetype));
}

function renderArtifact(artifact) {
	var table = document.getElementById("artifacts");
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/artifact.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(artifact.name));
	link.href = "/web/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(artifact.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(artifact.version));
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = "/web/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.onclick = function() {
		onClickUnregister(artifact.identifier);
	};
	cell.appendChild(button);
	row.appendChild(cell);

	table.appendChild(row);
}

function renderArchetype(archetypeId) {
	for (var i = 0; i != archetypes.length; i++) {
		if (archetypes[i].identifier == archetypeId) {
			var span = document.createElement("span");
			span.appendChild(document.createTextNode(archetypes[i].name));
			span.title = archetypes[i].identifier;
			return span;
		}
	}
	return document.createTextNode(archetypeId);
}

function onClickRegister() {
	if (!onOperationRequest("Registering artifact ..."))
		return false;
	var artifactId = document.getElementById("input-artifact").value;
	if (artifactId == null || artifactId == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.registerArtifactForCollaborationInput(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Registered artifact " + artifactId);
			waitAndRefresh();
		}
	}, specId, artifactId);
	return false;
}

function onClickUnregister(artifactId) {
	var result = confirm("Unregister artifact " + artifactId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Un-registering artifact ..."))
		return;
	xowl.unregisterArtifactForCollaborationInput(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Un-registered artifact " + artifactId);
			waitAndRefresh();
		}
	}, specId, artifactId);
	return;
}
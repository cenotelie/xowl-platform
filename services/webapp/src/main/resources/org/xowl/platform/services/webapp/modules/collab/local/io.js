// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var manifest = null;
var archetypes = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "Inputs & Outputs"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			manifest = content;
			if (archetypes !== null) {
				renderInputs(manifest.inputs);
				renderOutputs(manifest.outputs);
			}
		}
	});
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			archetypes = content;
			if (manifest !== null) {
				renderInputs(manifest.inputs);
				renderOutputs(manifest.outputs);
			}
		}
	});
}

function renderInputs(inputs) {
	inputs.sort(function (x, y) {
		return x.specification.name.localeCompare(y.specification.name);
	});
	var table = document.getElementById("inputs");
	for (var i = 0; i != inputs.length; i++) {
		(function (input) {
			var toRemove = function() { onClickRemoveInput(input.specification.identifier); };
			var row = renderIOElement(inputs[i], "io-input", toRemove);
			table.appendChild(row);
		})(inputs[i]);
	}
}

function renderOutputs(outputs) {
	outputs.sort(function (x, y) {
		return x.specification.name.localeCompare(y.specification.name);
	});
	var table = document.getElementById("outputs");
	for (var i = 0; i != outputs.length; i++) {
		(function (output) {
			var toRemove = function() { onClickRemoveInput(output.specification.identifier); };
			var row = renderIOElement(output, "io-output", toRemove);
			table.appendChild(row);
		})(outputs[i]);
	}
}

function renderIOElement(element, linkName, toRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/specification.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = element.specification.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(element.specification.name));
	link.href = linkName + ".html?id=" + encodeURIComponent(element.specification.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(renderArchetype(element.specification.archetype));
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(element.artifacts.length));
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = "/web/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.onclick = toRemove;
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
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

function onClickRemoveInput(specId) {
	var result = confirm("Remove input specification " + specId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Removing input specification ..."))
		return;
	xowl.removeCollaborationInputSpecification(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed input specification " + specId);
			waitAndRefresh();
		}
	}, specId);
	return;
}

function onClickRemoveOutput(specId) {
	var result = confirm("Remove output specification " + specId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Removing input specification ..."))
		return;
	xowl.removeCollaborationOutputSpecification(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed output specification " + specId);
			waitAndRefresh();
		}
	}, specId);
	return;
}
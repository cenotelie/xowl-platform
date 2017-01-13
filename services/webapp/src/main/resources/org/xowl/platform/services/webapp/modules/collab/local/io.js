// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "Inputs / Outputs"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderInputs(content.inputs);
			renderOutputs(content.outputs);
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
			var row = renderIOElement(inputs[i], "input", toRemove);
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
			var row = renderIOElement(output, "output", toRemove);
			table.appendChild(row);
		})(outputs[i]);
	}
}

function renderIOElement(element, link, toRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/specification.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(element.specification.name));
	link.href = link + ".html?id=" + encodeURIComponent(element.specification.identifier) + "&name=" + encodeURIComponent(element.specification.name);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(element.specification.archetype));
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(element.artifacts.length));
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
	button.onclick = toRemove;
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onClickRemoveInput(inputId) {

}

function onClickRemoveOutput(inputId) {

}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getCollaborationInputSpecifications(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderInputs(content);
		}
	});
	xowl.getCollaborationOutputSpecifications(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderOutputs(content);
		}
	});
}

function renderInputs(inputs) {
	inputs.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("inputs");
	for (var i = 0; i != inputs.length; i++) {
		(function (input) {
			var toRemove = function() { onClickRemoveInput(input.identifier); };
			var row = renderSpecification(inputs[i], "input", toRemove);
			table.appendChild(row);
		})(inputs[i]);
	}
}

function renderOutputs(outputs) {
	outputs.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("outputs");
	for (var i = 0; i != outputs.length; i++) {
		(function (output) {
			var toRemove = function() { onClickRemoveInput(output.identifier); };
			var row = renderSpecification(output, "output", toRemove);
			table.appendChild(row);
		})(outputs[i]);
	}
}

function renderSpecification(specification, link, toRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/specification.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(specification.name));
	link.href = link + ".html?id=" + encodeURIComponent(specification.identifier) + "&name=" + encodeURIComponent(specification.name);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(specification.archetype));
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
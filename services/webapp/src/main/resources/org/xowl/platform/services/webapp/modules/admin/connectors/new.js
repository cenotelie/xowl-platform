// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var descriptors = null;
var selectedDescriptor = null;
var archetypes = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Connectors Management", uri: ROOT + "/modules/admin/connectors/"},
			{name: "Spawn New"}], function() {
			doGetData();
	});
	document.getElementById("input-uri-addon").innerHTML = xowl.endpoint;
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getDescriptors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			descriptors = content;
			renderDescriptors(descriptors);
		}
	});
}

function renderDescriptors(descriptors) {
	descriptors.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("input-descriptor");
	while (select.length > 0)
		select.remove(select.length - 1);
	for (var i = 0; i != descriptors.length; i++) {
		var option = document.createElement("option");
		option.value = descriptors[i].identifier;
		option.appendChild(document.createTextNode(descriptors[i].name));
		select.appendChild(option);
	}
	select.selectedIndex = -1;
}

function onDescriptorSelect() {
	document.getElementById("input-descriptor-description").value = "";
	var params = document.getElementById("input-params");
	while (params.hasChildNodes()) {
		params.removeChild(params.lastChild);
	}

	var select = document.getElementById("input-descriptor");
	if (select.selectedIndex == -1)
		return;
	selectedDescriptor = descriptors[select.selectedIndex];
	document.getElementById("input-descriptor-description").value = selectedDescriptor.description;
	for (var i = 0; i != selectedDescriptor.parameters.length; i++) {
		var parameter = selectedDescriptor.parameters[i];
		params.appendChild(renderDescriptorParameter(parameter, i));
		if (parameter.typeHint === "archetype")
			setupAutocompleteArchetype("input-param-" + i);
	}
}

function renderDescriptorParameter(parameter, index) {
	var div = document.createElement("div");
	div.classList.add("form-group");

	var span = document.createElement("span");
	span.classList.add("col-sm-1");
	if (parameter.isRequired) {
		span.classList.add("glyphicon");
		span.classList.add("glyphicon-star");
		span.classList.add("text-danger");
		span.title = "REQUIRED";
	}
	div.appendChild(span);

	var label = document.createElement("label");
	label.classList.add("col-sm-2");
	label.classList.add("control-label");
	label.appendChild(document.createTextNode(parameter.name));
	div.appendChild(label);

	var content = document.createElement("div");
	content.classList.add("col-sm-9");
	content.classList.add("input-group");
	div.appendChild(content);

	var input = document.createElement("input");
	input.type = (parameter.typeHint === "password" ? "password" : "text");
	input.id = "input-param-" + index;
	input.classList.add("form-control");
	input.placeholder = parameter.placeholder;
	content.appendChild(input);

	if (parameter.typeHint === "archetype") {
		var addon = document.createElement("div");
		addon.classList.add("input-group-addon");
		var image = document.createElement("img");
		image.width = "20";
		image.height = "20";
		image.style.display = "none";
		image.id = "input-param-" + index + "-indicator";
		addon.appendChild(image);
		content.appendChild(addon);
	}
	return div;
}

function setupAutocompleteArchetype(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		if (archetypes !== null) {
			autocomplete.onItems(filterItems(archetypes, value));
			return;
		}
		xowl.getArtifactArchetypes(function (status, ct, content) {
			if (status === 200) {
				archetypes = content;
				autocomplete.onItems(filterItems(archetypes, value));
			}
		});
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

function filterItems(items, value) {
	var result = [];
	for (var i = 0; i != items.length; i++) {
		if (items[i].identifier.indexOf(value) >= 0 || items[i].name.indexOf(value) >= 0) {
			result.push(items[i]);
		}
	}
	return result;
}

function onClickNewConnector() {
	if (!onOperationRequest("Spawning new connector ..."))
		return false;
	var id = document.getElementById("input-id").value;
	var name = document.getElementById("input-name").value;
	var uri = document.getElementById("input-uri").value;
	if (selectedDescriptor == null
		|| id == null || id == ""
		|| name == null || name == ""
		|| uri == null || uri == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}

	var data = {
		"type": "org.xowl.platform.services.connection.ConnectorServiceData",
		"identifier": id,
		"name": name,
		"uris": [uri]
	};
	for (var i = 0; i != selectedDescriptor.parameters.length; i++) {
		var value = document.getElementById("input-param-" + i).value;
		if (typeof value == "undefined" || value == null || value == "") {
			if (selectedDescriptor.parameters[i].isRequired) {
				onOperationAbort("Parameter " + selectedDescriptor.parameters[i].name + " is required.");
				return;
			}
		} else {
			data[selectedDescriptor.parameters[i].identifier] = value;
		}
	}

	xowl.createConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Spawned connector ", content, "."]});
			waitAndGo("connector.html?id=" + encodeURIComponent(content.identifier));
		}
	}, selectedDescriptor, data);
}

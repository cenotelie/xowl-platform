// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var platformRoles = null;
var platformArchetypes = null;
var platformSpecifications = null;
var platformPatterns = null;

var specification = {
	name: "",
	inputs: [],
	outputs: [],
	roles: [],
	pattern: {}
};

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Collaborations Network", uri: "/web/modules/collab/network/"},
			{name: "New Collaboration"}], function() {
		setupAutocompleteArchetype("input-archetype");
		setupAutocompleteArchetype("output-archetype");
		setupAutocompleteSpecification("input-specification");
		setupAutocompleteSpecification("output-specification");
		setupAutocompleteRole("role-existing");
		setupAutocompletePattern("collaboration-pattern");
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			platformArchetypes = content;
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

function setupAutocompleteArchetype(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		autocomplete.onItems(filterItems(platformArchetypes, value));
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

function setupAutocompleteSpecification(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		if (platformSpecifications !== null) {
			autocomplete.onItems(filterItems(platformSpecifications, value));
			return;
		}
		xowl.getKnownIOSpecifications(function (status, ct, content) {
			if (status === 200) {
				platformSpecifications = content;
				autocomplete.onItems(filterItems(platformSpecifications, value));
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

function setupAutocompleteRole(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		if (platformRoles !== null) {
			autocomplete.onItems(filterItems(platformRoles, value));
			return;
		}
		xowl.getPlatformRoles(function (status, ct, content) {
			if (status === 200) {
				platformRoles = content;
				autocomplete.onItems(filterItems(platformRoles, value));
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

function setupAutocompletePattern(component) {
	var autocomplete = new AutoComplete(component);
	autocomplete.lookupItems = function (value) {
		if (platformPatterns !== null) {
			autocomplete.onItems(filterItems(platformPatterns, value));
			return;
		}
		xowl.getKnownPatterns(function (status, ct, content) {
			if (status === 200) {
				platformPatterns = content;
				autocomplete.onItems(filterItems(platformPatterns, value));
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

function onClickCreateInput() {
	var identifier = document.getElementById("input-identifier").value;
	var name = document.getElementById("input-name").value;
	var archetype = document.getElementById("input-archetype").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| archetype == null || archetype == "") {
		return;
	}
	var input = {
		identifier: identifier,
		name: name,
		archetype: archetype
	};
	doAddInput(input);
	document.getElementById("input-identifier").value = "";
	document.getElementById("input-name").value = "";
	document.getElementById("input-archetype").value = "";
}

function onClickAddInput() {
	var identifier = document.getElementById("input-specification").value;
	if (identifier == null || identifier == "")
		return;
	var input = null;
	for (var i = 0; i != platformSpecifications.length; i++) {
		if (platformSpecifications[i].identifier == identifier) {
			input = platformSpecifications[i];
			break;
		}
	}
	if (input == null)
		return;
	doAddInput(input);
	document.getElementById("input-specification").value = "";
}

function doAddInput(input) {
	specification.inputs.push(input);
	var table = document.getElementById("inputs");
	var row = null;
	var toRemove = function () {
		table.removeChild(row);
		specification.inputs.splice(specification.inputs.indexOf(input), 1);
	};
	row = renderSpecification(input, toRemove);
	table.appendChild(row);
}

function onClickCreateOutput() {
	var identifier = document.getElementById("output-identifier").value;
	var name = document.getElementById("output-name").value;
	var archetype = document.getElementById("output-archetype").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| archetype == null || archetype == "") {
		return;
	}
	var output = {
		identifier: identifier,
		name: name,
		archetype: archetype
	};
	doAddOutput(output);
	document.getElementById("output-identifier").value = "";
	document.getElementById("output-name").value = "";
	document.getElementById("output-archetype").value = "";
}

function onClickAddOutput() {
	var identifier = document.getElementById("output-specification").value;
	if (identifier == null || identifier == "")
		return;
	var output = null;
	for (var i = 0; i != platformSpecifications.length; i++) {
		if (platformSpecifications[i].identifier == identifier) {
			output = platformSpecifications[i];
			break;
		}
	}
	if (output == null)
		return;
	doAddOutput(output);
	document.getElementById("output-specification").value = "";
}

function doAddOutput(output) {
	specification.outputs.push(output);
	var table = document.getElementById("outputs");
	var row = null;
	var toRemove = function () {
		table.removeChild(row);
		specification.outputs.splice(specification.outputs.indexOf(output), 1);
	};
	row = renderSpecification(output, toRemove);
	table.appendChild(row);
}

function onClickCreateRole() {
	var identifier = document.getElementById("role-identifier").value;
	var name = document.getElementById("role-name").value;
	if (identifier == null || identifier == ""
		|| name == null || name == "") {
		return;
	}
	var role = {
		identifier: identifier,
		name: name
	};
	doAddRole(role);
	document.getElementById("role-identifier").value = "";
	document.getElementById("role-name").value = "";
}

function onClickAddRole() {
	var identifier = document.getElementById("role-existing").value;
	if (identifier == null || identifier == "")
		return;
	var role = null;
	for (var i = 0; i != platformRoles.length; i++) {
		if (platformRoles[i].identifier == identifier) {
			role = platformRoles[i];
			break;
		}
	}
	if (role == null)
		return;
	doAddRole(role);
	document.getElementById("role-existing").value = "";
}

function doAddRole(role) {
	specification.roles.push(role);
	var table = document.getElementById("roles");
	var row = null;
	var toRemove = function () {
		table.removeChild(row);
		specification.roles.splice(specification.roles.indexOf(role), 1);
	};
	row = renderRole(role, toRemove);
	table.appendChild(row);
}

function onClickSpawn() {
}




function renderSpecification(specification, toRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/specification.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = specification.identifier;
	var span = document.createElement("span");
	span.appendChild(document.createTextNode(specification.name));
	span.title = specification.identifier;
	cell.appendChild(image);
	cell.appendChild(span);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(renderArchetype(specification.archetype));
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
	for (var i = 0; i != platformArchetypes.length; i++) {
		if (platformArchetypes[i].identifier == archetypeId) {
			var span = document.createElement("span");
			span.appendChild(document.createTextNode(platformArchetypes[i].name));
			span.title = platformArchetypes[i].identifier;
			return span;
		}
	}
	return document.createTextNode(archetypeId);
}

function renderRole(role, toRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = role.identifier;
	var span = document.createElement("span");
	span.appendChild(document.createTextNode(role.name));
	span.title = role.identifier;
	cell.appendChild(image);
	cell.appendChild(span);
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
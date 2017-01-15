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
		if (platformArchetypes !== null) {
			autocomplete.onItems(filterItems(platformArchetypes, value));
			return;
		}
		xowl.getArtifactArchetypes(function (status, ct, content) {
			if (status === 200) {
				platformArchetypes = content;
				autocomplete.onItems(filterItems(platformArchetypes, value));
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
		xowl.getPlatformRole(function (status, ct, content) {
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
	specification.inputs.push(input);
}

function onClickAddInput() {
}

function onClickCreateOutput() {
}

function onClickAddOutput() {
}

function onClickCreateRole() {
}

function onClickAddRole() {
}

function onClickSpawn() {
}
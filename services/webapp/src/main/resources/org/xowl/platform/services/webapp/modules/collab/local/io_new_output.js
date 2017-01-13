// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var archetypes = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "Inputs & Outputs", uri: "/web/modules/collab/local/io.html"},
			{name: "New Output Spec ..."}], function() {
		setupAutocomplete();
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("input-archetype");
	autocomplete1.lookupItems = function (value) {
		if (archetypes !== null) {
			autocomplete1.onItems(filterItems(archetypes, value));
			return;
		}
		xowl.getArtifactArchetypes(function (status, ct, content) {
			if (status === 200) {
				archetypes = content;
				autocomplete1.onItems(filterItems(archetypes, value));
			}
		});
	};
	autocomplete1.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
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

function onClickCreate() {
	if (!onOperationRequest("Creating new output ..."))
		return false;
	var identifier = document.getElementById("input-identifier").value;
	var name = document.getElementById("input-name").value;
	var archetype = document.getElementById("input-archetype").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| archetype == null || archetype == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.addCollaborationOutputSpecification(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Created output " + identifier);
			waitAndGo("io.html");
		}
	}, {identifier: identifier, name: name, archetype: archetype});
	return false;
}
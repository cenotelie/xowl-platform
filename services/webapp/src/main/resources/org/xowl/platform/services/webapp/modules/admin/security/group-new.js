// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var users = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "New Group"}], function() {
			setupAutocomplete();
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("group-admin");
	autocomplete1.lookupItems = function (value) {
		if (users !== null) {
			autocomplete1.onItems(filterItems(users, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				users = content;
				autocomplete1.onItems(filterItems(users, value));
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

function create() {
	if (!onOperationRequest("Creating new group ..."))
		return false;
	var identifier = document.getElementById("group-identifier").value;
	var name = document.getElementById("group-name").value;
	var admin = document.getElementById("group-admin").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| admin == null || admin == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.createPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created group ", content, "."]});
			waitAndGo("group.html?id=" + encodeURIComponent(content.identifier));
		}
	}, identifier, name, admin);
	return false;
}
// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var users = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community"}], function() {
		setupAutocomplete();
	});
}

function setupAutocomplete() {
	var autocomplete = new AutoComplete("input-user");
	autocomplete.lookupItems = function (value) {
		if (users !== null) {
			autocomplete.onItems(filterItems(users, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				users = content;
				autocomplete.onItems(filterItems(users, value));
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
	autocomplete.onItemSelected = function (item) {
		window.location.href = "profile-view.html?id=" + encodeURIComponent(item.identifier);
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
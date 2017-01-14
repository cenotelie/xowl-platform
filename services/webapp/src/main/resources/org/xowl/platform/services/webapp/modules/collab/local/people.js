// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var manifest = null;
var roles = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "People and Roles"}], function() {
		setupAutocomplete();
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			manifest = content;
			if (roles !== null) {
				renderRoles(manifest.inputs);
			}
		}
	});
	xowl.getPlatformRoles(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			roles = content;
			if (manifest !== null) {
				renderRoles(manifest.inputs);
			}
		}
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("input-role");
	autocomplete1.lookupItems = function (value) {
		autocomplete1.onItems(filterItems(roles, value));
		return;
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

function renderRoles() {
	manifest.roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var i = 0; i != manifest.roles.length; i++) {
		table.appendChild(renderRole(manifest.roles[i]));
	}
}

function renderRole(roleId) {
	var role = getRoleObject(roleId);
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role != null ? role.name : roleId));
	link.href = "/web/modules/admin/security/role.html.html?id=" + encodeURIComponent(roleId);
	cell.appendChild(image);
	cell.appendChild(link);
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
		onClickRemoveRole(roleId);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function getRoleObject(roleId) {
	for (var i = 0; i != roles.length; i++) {
		if (roles[i].identifier == roleId) {
			return roles[i];
		}
	}
	return null;
}

function onClickRemoveRole(roleId) {
	var result = confirm("Remove role " + roleId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Removing role ..."))
		return;
	xowl.removeCollaborationRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed role " + roleId);
			waitAndRefresh();
		}
	}, roleId);
	return;
}

function onAddRole() {
	if (!onOperationRequest("Adding role ..."))
		return false;
	var roleId = document.getElementById("input-role").value;
	if (roleId == null || roleId == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.addCollaborationRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added role " + roleId);
			waitAndRefresh();
		}
	}, roleId);
	return false;
}
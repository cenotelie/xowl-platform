// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
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
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderRoles(content.roles);
		}
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("input-role");
	autocomplete1.lookupItems = function (value) {
		if (roles !== null) {
			autocomplete1.onItems(filterItems(roles, value));
			return;
		}
		xowl.getPlatformRoles(function (status, ct, content) {
			if (status === 200) {
				roles = content;
				autocomplete1.onItems(filterItems(roles, value));
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

function renderRoles(roles) {
	roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var i = 0; i != roles.length; i++) {
		table.appendChild(renderRole(roles[i]));
	}
}

function renderRole(role) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href = "/web/modules/admin/security/role.html.html?id=" + encodeURIComponent(role.identifier);
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
		onClickRemoveRole(role.identifier);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
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
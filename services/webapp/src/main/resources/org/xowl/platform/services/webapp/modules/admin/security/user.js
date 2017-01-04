// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var userId = getParameterByName("id");
var oldName = null;
var roles = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "User " + userId}], function() {
		if (!userId || userId === null || userId === "")
			return;
		setupAutocomplete();
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderUser(content);
			}
		}, userId);
	});
}

function setupAutocomplete() {
	var autocomplete3 = new AutoComplete("input-role");
	autocomplete3.lookupItems = function (value) {
		if (roles !== null) {
			autocomplete3.onItems(filterItems(roles, value));
			return;
		}
		xowl.getPlatformRoles(function (status, ct, content) {
			if (status === 200) {
				roles = content;
				autocomplete3.onItems(filterItems(roles, value));
			}
		});
	};
	autocomplete3.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete3.getItemString = function (item) {
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

function renderUser(user) {
	document.getElementById("user-identifier").value = user.identifier;
	document.getElementById("user-name").value = user.name;
	user.roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != user.roles.length; i++) {
		table.appendChild(renderPlatformRole(user.roles[i]));
	}
}

function renderPlatformRole(role) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/role.svg";
	image.width = 30
	image.height = 30
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href="role.html?id=" + encodeURIComponent(role.identifier);
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
	button.onclick = function () {
		onRemoveRole(role.identifier);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onClickEdit() {
	if (oldName !== null)
		return;
	document.getElementById("user-name-edit").style.display = "none";
	document.getElementById("user-name-validate").style.display = "inline-block";
	document.getElementById("user-name-cancel").style.display = "inline-block";
	document.getElementById("user-name").readOnly = false;
	document.getElementById("user-name").focus();
	document.getElementById("user-name").select();
	oldName = document.getElementById("user-name").value;
}

function onClickValidate() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").readOnly = true;
	if (!onOperationRequest("Renaming ..."))
		return;
	xowl.renamePlatformUser(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("user-name").value = oldName;
		}
		oldName = null;
	}, userId, document.getElementById("user-name").value);
}

function onClickCancel() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").value = oldName;
	document.getElementById("user-name").readOnly = true;
	oldName = null;
}

function onClickDelete() {
	var result = confirm("Delete the user " + userId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting this user ..."))
		return;
	xowl.deletePlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Deleted user " + userId + ".");
			waitAndGo("index.html");
		}
	}, userId);
}

function onClickResetPassword() {
	var result = confirm("Reset password for user " + userId + "?");
	if (!result)
		return;
	var newPassword = document.getElementById("user-new-password").value;
	if (!onOperationRequest("Resetting password ..."))
		return;
	xowl.resetPlatformUserPassword(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Password has been reset.");
			waitAndRefresh();
		}
	}, userId, newPassword);
}

function onAddRole() {
	var roleId = document.getElementById("input-role").value;
	if (roleId == null || roleId == "")
		return;
	if (!onOperationRequest("Assigning role " + roleId + " ..."))
		return;
	xowl.assignRoleToPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Assigned role " + roleId + ".");
			waitAndRefresh();
		}
	}, roleId, userId);
}

function onRemoveRole(roleId) {
	var result = confirm("Un-assign role " + roleId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Un-assigning role " + roleId + " ..."))
		return;
	xowl.unassignRoleFromPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Un-assigned role " + roleId + ".");
			waitAndRefresh();
		}
	}, roleId, userId);
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var userId = getParameterByName("id");
var user = null;
var oldName = null;
var roles = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Security", uri: ROOT + "/modules/admin/security/"},
			{name: "User " + userId}], function() {
		if (!userId || userId === null || userId === "")
			return;
		setupAutocomplete();
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				user = content;
				renderUser();
			}
		}, userId);
	});
}

function setupAutocomplete() {
	var autocomplete = new AutoComplete("input-role");
	autocomplete.lookupItems = function (value) {
		if (roles !== null) {
			autocomplete.onItems(filterItems(roles, value));
			return;
		}
		xowl.getPlatformRoles(function (status, ct, content) {
			if (status === 200) {
				roles = content;
				autocomplete.onItems(filterItems(roles, value));
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

function renderUser() {
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
	image.src = ROOT + "/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = role.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href="role.html?id=" + encodeURIComponent(role.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.onclick = function () {
		onRemoveRole(role);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}


function onClickDelete() {
	popupConfirm("Platform Security", richString(["Delete user ", user, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting user ", user, " ..."])))
			return;
		xowl.deletePlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Deleted user ", user, "."]));
				waitAndGo("index.html");
			}
		}, userId);
	});
}

function onClickNameEdit() {
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

function onClickNameValidate() {
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

function onClickNameCancel() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").value = oldName;
	document.getElementById("user-name").readOnly = true;
	oldName = null;
}

function onPopupResetPasswordOpen() {
	showPopup("popup-reset-password");
}

function onPopupResetPasswordOk() {
	if (!onOperationRequest("Resetting password ..."))
		return;
	var newPassword = document.getElementById("user-new-password").value;
	if (newPassword == null || newPassword == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	hidePopup("popup-reset-password");
	xowl.resetPlatformUserPassword(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Password has been reset.");
			waitAndRefresh();
		}
	}, userId, newPassword);
}

function onPopupResetPasswordCancel() {
	hidePopup("popup-reset-password");
	document.getElementById("user-new-password").value = "";
}

function onPopupAssignRoleOpen() {
	showPopup("popup-assign-role");
}

function onPopupAssignRoleOk() {
	if (!onOperationRequest("Assigning role ..."))
		return;
	var roleId = document.getElementById("input-role").value;
	if (roleId == null || roleId == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	hidePopup("popup-assign-role");
	xowl.assignRoleToPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Assigned role " + roleId + ".");
			waitAndRefresh();
		}
	}, roleId, userId);
}

function onPopupAssignRoleCancel() {
	hidePopup("popup-assign-role");
	document.getElementById("input-role").value = "";
}

function onRemoveRole(role) {
	popupConfirm("Platform Security", richString(["Un-assign role " , role, "?"]), function () {
		if (!onOperationRequest(richString(["Un-assigning role " , role, " ..."])))
			return;
		xowl.unassignRoleFromPlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Un-assigned role " , role, "."]));
				waitAndRefresh();
			}
		}, role.identifier, userId);
	});
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var groupId = getParameterByName("id");
var oldName = null;
var users = null;
var roles = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "Group " + groupId}], function() {
		if (!groupId || groupId === null || groupId === "")
			return;
		setupAutocomplete();
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformGroup(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderGroup(content);
			}
		}, groupId);
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("input-admin");
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
	var autocomplete2 = new AutoComplete("input-member");
	autocomplete2.lookupItems = function (value) {
		if (users !== null) {
			autocomplete2.onItems(filterItems(users, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				users = content;
				autocomplete2.onItems(filterItems(users, value));
			}
		});
	};
	autocomplete2.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete2.getItemString = function (item) {
		return item.identifier;
	};
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

function renderGroup(group) {
	document.getElementById("group-identifier").value = group.identifier;
	document.getElementById("group-name").value = group.name;
	group.admins.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	group.members.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	group.roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("admins");
	for (var  i = 0; i != group.admins.length; i++) {
		(function(user) {
			var onClickRemove = function() { onRemoveAdmin(user.identifier); };
			table.appendChild(renderPlatformUser(user, onClickRemove));
		})(group.admins[i]);
	}
	table = document.getElementById("members");
	for (var  i = 0; i != group.members.length; i++) {
		(function(user) {
			var onClickRemove = function() { onRemoveMember(user.identifier); };
			table.appendChild(renderPlatformUser(user, onClickRemove));
		})(group.members[i]);
	}
	table = document.getElementById("roles");
	for (var  i = 0; i != group.roles.length; i++) {
		table.appendChild(renderPlatformRole(group.roles[i]));
	}
}

function renderPlatformUser(user, onClickRemove) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/user.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = user.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(user.name));
	link.href="user.html?id=" + encodeURIComponent(user.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
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
	button.onclick = onClickRemove;
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function renderPlatformRole(role) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/role.svg";
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
	image.src = "/web/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "REMOVE";
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
	document.getElementById("group-name-edit").style.display = "none";
	document.getElementById("group-name-validate").style.display = "inline-block";
	document.getElementById("group-name-cancel").style.display = "inline-block";
	document.getElementById("group-name").readOnly = false;
	document.getElementById("group-name").focus();
	document.getElementById("group-name").select();
	oldName = document.getElementById("group-name").value;
}

function onClickValidate() {
	document.getElementById("group-name-edit").style.display = "inline-block";
	document.getElementById("group-name-validate").style.display = "none";
	document.getElementById("group-name-cancel").style.display = "none";
	document.getElementById("group-name").readOnly = true;
	if (!onOperationRequest("Renaming ..."))
		return;
	xowl.renamePlatformGroup(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("group-name").value = oldName;
		}
		oldName = null;
	}, groupId, document.getElementById("group-name").value);
}

function onClickCancel() {
	document.getElementById("group-name-edit").style.display = "inline-block";
	document.getElementById("group-name-validate").style.display = "none";
	document.getElementById("group-name-cancel").style.display = "none";
	document.getElementById("group-name").value = oldName;
	document.getElementById("group-name").readOnly = true;
	oldName = null;
}

function onClickDelete() {
	var result = confirm("Delete the group " + groupId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting this group ..."))
		return;
	xowl.deletePlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Deleted group " + roleId + ".");
			waitAndGo("index.html");
		}
	}, groupId);
}

function onAddAdmin() {
	var userId = document.getElementById("input-admin").value;
	if (userId == null || userId == "")
		return;
	if (!onOperationRequest("Adding group administrator" + userId + " ..."))
		return;
	xowl.addAdminToPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added group administrator " + userId + ".");
			waitAndRefresh();
		}
	}, groupId, userId);
}

function onAddMember() {
	var userId = document.getElementById("input-member").value;
	if (userId == null || userId == "")
		return;
	if (!onOperationRequest("Adding group member" + userId + " ..."))
		return;
	xowl.addMemberToPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added group member " + userId + ".");
			waitAndRefresh();
		}
	}, groupId, userId);
}

function onAddRole() {
	var roleId = document.getElementById("input-role").value;
	if (roleId == null || roleId == "")
		return;
	if (!onOperationRequest("Assigning role " + roleId + " ..."))
		return;
	xowl.assignRoleToPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Assigned role " + roleId + ".");
			waitAndRefresh();
		}
	}, roleId, groupId);
}

function onRemoveAdmin(userId) {
	var result = confirm("Remove group administrator " + userId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Removing group administrator " + userId + " ..."))
		return;
	xowl.removeAdminFromPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed group administrator " + userId + ".");
			waitAndRefresh();
		}
	}, groupId, userId);
}

function onRemoveMember(userId) {
	var result = confirm("Remove group member " + userId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Removing group member " + userId + " ..."))
		return;
	xowl.removeMemberFromPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed group member " + userId + ".");
			waitAndRefresh();
		}
	}, groupId, userId);
}

function onRemoveRole(roleId) {
	var result = confirm("Un-assign role " + roleId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Un-assigning role " + roleId + " ..."))
		return;
	xowl.unassignRoleFromPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Un-assigned role " + roleId + ".");
			waitAndRefresh();
		}
	}, roleId, groupId);
}
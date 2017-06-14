// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var USERS = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Security"}], function() {
		setupAutocomplete();
		doGetData();
	});
}

function setupAutocomplete() {
	var autocomplete1 = new AutoComplete("popup-new-group-admin");
	autocomplete1.lookupItems = function (value) {
		if (USERS !== null) {
			autocomplete1.onItems(filterItems(USERS, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				USERS = content;
				autocomplete1.onItems(filterItems(USERS, value));
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

function doGetData() {
	if (!onOperationRequest("Loading ...", 3))
		return;
	xowl.getPlatformUsers(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			USERS = content;
			renderPlatformUsers(content);
		}
	});
	xowl.getPlatformGroups(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderPlatformGroups(content);
		}
	});
	xowl.getPlatformRoles(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderPlatformRoles(content);
		}
	});
}

function renderPlatformUsers(users) {
	users.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("users");
	for (var  i = 0; i != users.length; i++) {
		table.appendChild(renderPlatformUser(users[i]));
	}
}

function renderPlatformGroups(groups) {
	groups.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("groups");
	for (var  i = 0; i != groups.length; i++) {
		table.appendChild(renderPlatformGroup(groups[i]));
	}
}

function renderPlatformRoles(roles) {
	roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != roles.length; i++) {
		table.appendChild(renderPlatformRole(roles[i]));
	}
}

function renderPlatformUser(user) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/user.svg";
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
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onClickDeleteUser(user);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function renderPlatformGroup(group) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/group.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = group.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(group.name));
	link.href="group.html?id=" + encodeURIComponent(group.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onClickDeleteGroup(group);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
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
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onClickDeleteRole(role);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}


function onPopupNewUserOpen() {
	showPopup("popup-new-user");
}

function onPopupNewUserOk() {
	if (!onOperationRequest("Creating new user ..."))
		return false;
	var identifier = document.getElementById("popup-new-user-identifier").value;
	var name = document.getElementById("popup-new-user-name").value;
	var password1 = document.getElementById("popup-new-user-password1").value;
	var password2 = document.getElementById("popup-new-user-password2").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| password1 == null || password1 == ""
		|| password2 == null || password2 == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	if (password1 !== password2) {
		onOperationAbort("The two passwords are different.");
		return false;
	}
	hidePopup("popup-new-user");
	xowl.createPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", richString(["Created user ", content, "."]));
			waitAndRefresh();
		}
	}, identifier, name, password1);
}

function onPopupNewUserCancel() {
	hidePopup("popup-new-user");
	document.getElementById("popup-new-user-identifier").value = "";
	document.getElementById("popup-new-user-name").value = "";
	document.getElementById("popup-new-user-password1").value = "";
	document.getElementById("popup-new-user-password2").value = "";
}

function onPopupNewGroupOpen() {
	showPopup("popup-new-group");
}

function onPopupNewGroupOk() {
	if (!onOperationRequest("Creating new group ..."))
		return false;
	var identifier = document.getElementById("popup-new-group-identifier").value;
	var name = document.getElementById("popup-new-group-name").value;
	var admin = document.getElementById("popup-new-group-admin").value;
	if (identifier == null || identifier == "" || name == null || name == "" || admin == null || admin == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	hidePopup("popup-new-group");
	xowl.createPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", richString(["Created group ", content, "."]));
			waitAndRefresh();
		}
	}, identifier, name, admin);
}

function onPopupNewGroupCancel() {
	hidePopup("popup-new-group");
	document.getElementById("popup-new-group-identifier").value = "";
	document.getElementById("popup-new-group-name").value = "";
	document.getElementById("popup-new-group-admin").value = "";
}

function onPopupNewRoleOpen() {
	showPopup("popup-new-role");
}

function onPopupNewRoleOk() {
	if (!onOperationRequest("Creating new role ..."))
		return false;
	var identifier = document.getElementById("popup-new-role-identifier").value;
	var name = document.getElementById("popup-new-role-name").value;
	if (identifier == null || identifier == "" || name == null || name == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	hidePopup("popup-new-role");
	xowl.createPlatformRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", richString(["Created role ", content, "."]));
			waitAndRefresh();
		}
	}, identifier, name);
}

function onPopupNewRoleCancel() {
	hidePopup("popup-new-role");
	document.getElementById("popup-new-role-identifier").value = "";
	document.getElementById("popup-new-role-name").value = "";
}

function onClickDeleteUser(user) {
	popupConfirm("Platform Security", richString(["Delete user ", user, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting user ", user, " ..."])))
			return;
		xowl.deletePlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Deleted user ", user, "."]));
				waitAndRefresh();
			}
		}, user.identifier);
	});
}

function onClickDeleteGroup(group) {
	popupConfirm("Platform Security", richString(["Delete group ", group, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting group", group, " ..."])))
			return;
		xowl.deletePlatformGroup(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Deleted group", group, "."]));
				waitAndRefresh();
			}
		}, group.identifier);
	});
}

function onClickDeleteRole(role) {
	popupConfirm("Platform Security", richString(["Delete role ", role, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting role ", role, " ..."])))
			return;
		xowl.deletePlatformRole(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Deleted role ", role, "."]));
				waitAndRefresh();
			}
		}, role.identifier);
	});
}
// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var RESOURCE = null;
var USERS = null;
var GROUPS = null;
var ROLES = null;

function setupSRAutocomplete() {
	var fieldOwner = new AutoComplete("input-owner");
	fieldOwner.lookupItems = function (value) {
		if (USERS !== null) {
			fieldOwner.onItems(filterItems(USERS, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				USERS = content;
				fieldOwner.onItems(filterItems(USERS, value));
			}
		});
	};
	fieldOwner.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	fieldOwner.getItemString = function (item) {
		return item.identifier;
	};

	var fieldSharingUser = new AutoComplete("input-sharing-user");
	fieldSharingUser.lookupItems = function (value) {
		if (USERS !== null) {
			fieldSharingUser.onItems(filterItems(USERS, value));
			return;
		}
		xowl.getPlatformUsers(function (status, ct, content) {
			if (status === 200) {
				USERS = content;
				fieldSharingUser.onItems(filterItems(USERS, value));
			}
		});
	};
	fieldSharingUser.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	fieldSharingUser.getItemString = function (item) {
		return item.identifier;
	};

	var fieldSharingGroup = new AutoComplete("input-sharing-group");
	fieldSharingGroup.lookupItems = function (value) {
		if (GROUPS !== null) {
			fieldSharingGroup.onItems(filterItems(GROUPS, value));
			return;
		}
		xowl.getPlatformGroups(function (status, ct, content) {
			if (status === 200) {
				GROUPS = content;
				fieldSharingGroup.onItems(filterItems(GROUPS, value));
			}
		});
	};
	fieldSharingGroup.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	fieldSharingGroup.getItemString = function (item) {
		return item.identifier;
	};

	var fieldSharingRole = new AutoComplete("input-sharing-role");
	fieldSharingRole.lookupItems = function (value) {
		if (ROLES !== null) {
			fieldSharingRole.onItems(filterItems(ROLES, value));
			return;
		}
		xowl.getPlatformRoles(function (status, ct, content) {
			if (status === 200) {
				ROLES = content;
				fieldSharingRole.onItems(filterItems(ROLES, value));
			}
		});
	};
	fieldSharingRole.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	fieldSharingRole.getItemString = function (item) {
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

function renderDescriptorIcon(descriptor) {
	var icon = document.createElement("img");
	if (descriptor == null) {
		// shared with the current user
		icon.src = ROOT + "/assets/resource-shared.svg";
		icon.title = "SHARED WITH ME";
	} else if (descriptor.sharing.length > 0) {
		// shared by the current user with others
		icon.src = ROOT + "/assets/resource-shared.svg";
		icon.title = "SHARED BY ME";
	} else if (descriptor.owners.length > 1) {
		// multiple owners for this resource
		icon.src = ROOT + "/assets/resource-multiowner.svg";
		icon.title = "CO-OWNED";
	} else {
		// the current use is the sole owner
		icon.src = ROOT + "/assets/resource-private.svg";
		icon.title = "PRIVATE";
	}
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	return icon;
}

function renderDescriptor(descriptor) {
	descriptor.owners.sort(function (x, y) {
		return x.localeCompare(y);
	});
	var table = document.getElementById("descriptor-owners");
	for (var  i = 0; i != descriptor.owners.length; i++) {
		table.appendChild(renderPlatformUser(descriptor.owners[i]));
	}
	table = document.getElementById("descriptor-sharing");
	for (var  i = 0; i != descriptor.sharing.length; i++) {
		table.appendChild(renderSharing(descriptor.sharing[i]));
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
	image.title = user;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(user));
	link.href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(user);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onPopupRemoveOwnerOpen(user);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function renderSharingString(sharing) {
	if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithEverybody") {
		return "Sharing with Everybody";
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithUser") {
		return "Sharing with user " + sharing.user;
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithGroup") {
		return "Sharing with group " + sharing.group;
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithRole") {
		return "Sharing with role " + sharing.role;
	}
	return "";
}

function renderSharing(sharing) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithEverybody") {
		image.src = ROOT + "/assets/resource-shared.svg";
		image.title = "EVERYBODY";
		link.appendChild(document.createTextNode("Everybody"));
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithUser") {
		image.src = ROOT + "/assets/user.svg";
		image.title = sharing.user;
		link.appendChild(document.createTextNode(sharing.user));
		link.href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(sharing.user);
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithGroup") {
		image.src = ROOT + "/assets/group.svg";
		image.title = sharing.group;
		link.appendChild(document.createTextNode(sharing.group));
		link.href = ROOT + "/modules/admin/security/group.html?id=" + encodeURIComponent(sharing.group);
	} else if (sharing.type === "org.xowl.platform.kernel.security.SecuredResourceSharingWithRole") {
		image.src = ROOT + "/assets/role.svg";
		image.title = sharing.role;
		link.appendChild(document.createTextNode(sharing.role));
		link.href = ROOT + "/modules/admin/security/role.html?id=" + encodeURIComponent(sharing.role);
	}

	cell = document.createElement("td");
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onPopupRemoveSharingOpen(sharing);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onPopupNewOwnerOpen() {
	showPopup("popup-new-owner");
}

function onPopupNewOwnerCancel() {
	hidePopup("popup-new-owner");
	document.getElementById("input-owner").value = "";
}

function onPopupNewOwnerAdd() {
	hidePopup("popup-new-owner");
	var userId = document.getElementById("input-owner").value;
	if (userId === null || userId === "")
		return;
	if (!onOperationRequest("Adding user " + userId + " as new owner ..."))
		return;
	xowl.addSecuredResourceOwner(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added user " + userId + " as new owner.");
			waitAndRefresh();
		}
	}, RESOURCE, userId);
}

var USER_TO_REMOVE = null;

function onPopupRemoveOwnerOpen(user) {
	USER_TO_REMOVE = user;
	var placeholder = document.getElementById("popup-remove-owner-name");
	while (placeholder.hasChildNodes()) {
		placeholder.removeChild(placeholder.lastChild);
	}
	placeholder.appendChild(document.createTextNode(user));
	showPopup("popup-remove-owner");
}

function onPopupRemoveOwnerCancel() {
	hidePopup("popup-remove-owner");
}

function onPopupRemoveOwnerOK() {
	hidePopup("popup-remove-owner");
	if (!onOperationRequest("Removing user " + USER_TO_REMOVE + " as owner ..."))
		return;
	xowl.removeSecuredResourceOwner(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed user " + USER_TO_REMOVE + " as owner.");
			waitAndRefresh();
		}
	}, RESOURCE, USER_TO_REMOVE);
}

function onPopupNewSharingWithEverybodyOpen() {
	showPopup("popup-new-sharing-everybody");
}

function onPopupNewSharingWithEverybodyCancel() {
	hidePopup("popup-new-sharing-everybody");
}

function onPopupNewSharingWithEverybodyAdd() {
	hidePopup("popup-new-sharing-everybody");
	if (!onOperationRequest("Adding a sharing with everybody ..."))
		return;
	xowl.addSecuredResourceSharing(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added a sharing with everybody.");
			waitAndRefresh();
		}
	}, RESOURCE, {"type": "org.xowl.platform.kernel.security.SecuredResourceSharingWithEverybody"});
}

function onPopupNewSharingWithUserOpen() {
	showPopup("popup-new-sharing-user");
}

function onPopupNewSharingWithUserCancel() {
	hidePopup("popup-new-sharing-user");
	document.getElementById("input-sharing-user").value = "";
}

function onPopupNewSharingWithUserAdd() {
	hidePopup("popup-new-sharing-user");
	var userId = document.getElementById("input-sharing-user").value;
	if (userId === null || userId === "")
		return;
	if (!onOperationRequest("Adding sharing with user " + userId + " ..."))
		return;
	xowl.addSecuredResourceSharing(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added sharing with user " + userId + ".");
			waitAndRefresh();
		}
	}, RESOURCE, {
		"type": "org.xowl.platform.kernel.security.SecuredResourceSharingWithUser",
		"user": userId
	});
}

function onPopupNewSharingWithGroupOpen() {
	showPopup("popup-new-sharing-group");
}

function onPopupNewSharingWithGroupCancel() {
	hidePopup("popup-new-sharing-group");
	document.getElementById("input-sharing-group").value = "";
}

function onPopupNewSharingWithGroupAdd() {
	hidePopup("popup-new-sharing-group");
	var groupId = document.getElementById("input-sharing-group").value;
	if (groupId === null || groupId === "")
		return;
	if (!onOperationRequest("Adding sharing with group " + groupId + " ..."))
		return;
	xowl.addSecuredResourceSharing(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added sharing with group " + groupId + ".");
			waitAndRefresh();
		}
	}, RESOURCE, {
		"type": "org.xowl.platform.kernel.security.SecuredResourceSharingWithGroup",
		"group": groupId
	});
}

function onPopupNewSharingWithRoleOpen() {
	showPopup("popup-new-sharing-role");
}

function onPopupNewSharingWithRoleCancel() {
	hidePopup("popup-new-sharing-role");
	document.getElementById("input-sharing-role").value = "";
}

function onPopupNewSharingWithRoleAdd() {
	hidePopup("popup-new-sharing-role");
	var roleId = document.getElementById("input-sharing-role").value;
	if (roleId === null || roleId === "")
		return;
	if (!onOperationRequest("Adding sharing with role " + roleId + " ..."))
		return;
	xowl.addSecuredResourceSharing(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Added sharing with role " + roleId + ".");
			waitAndRefresh();
		}
	}, RESOURCE, {
		"type": "org.xowl.platform.kernel.security.SecuredResourceSharingWithRole",
		"role": roleId
	});
}

var SHARING_TO_REMOVE = null;

function onPopupRemoveSharingOpen(sharing) {
	SHARING_TO_REMOVE = sharing;
	var placeholder = document.getElementById("popup-remove-sharing-description");
	while (placeholder.hasChildNodes()) {
		placeholder.removeChild(placeholder.lastChild);
	}
	placeholder.appendChild(document.createTextNode(renderSharingString(sharing)));
	showPopup("popup-remove-sharing");
}

function onPopupRemoveSharingCancel() {
	hidePopup("popup-remove-sharing");
}

function onPopupRemoveSharingOK() {
	hidePopup("popup-remove-sharing");
	if (!onOperationRequest("Removing " + renderSharingString(SHARING_TO_REMOVE) + " ..."))
		return;
	xowl.removeSecuredResourceSharing(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Removed " + renderSharingString(SHARING_TO_REMOVE) + ".");
			waitAndRefresh();
		}
	}, RESOURCE, SHARING_TO_REMOVE);
}
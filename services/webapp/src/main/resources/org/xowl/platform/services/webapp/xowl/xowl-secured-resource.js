// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var USERS = null;
var RESOURCE = null;

function setupSRAutocomplete() {
	var autocomplete1 = new AutoComplete("input-owner");
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
	document.getElementById("popup-remove-owner-name").appendChild(document.createTextNode(user));
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
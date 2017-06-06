// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

function prepareSecuredResource() {
	loadComponent(ROOT + "/components/secured-resource-popups.html", function (node) {
		document.getElementById("placeholder-secured-resource-popups").appendChild(node);
	});
	loadComponent(ROOT + "/components/secured-resource-descriptor.html", function (node) {
		document.getElementById("placeholder-secured-resource-descriptor").appendChild(node);
	});
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
		return x.name.localeCompare(y.name);
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
	}, doc.identifier, userId);
}
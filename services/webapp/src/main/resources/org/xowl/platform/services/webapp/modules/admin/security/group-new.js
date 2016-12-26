// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "New Group"}], function() {
			doGetUsers();
	});
}

function doGetUsers() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getPlatformUsers(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderPlatformUsers(content);
		}
	});
}

function renderPlatformUsers(users) {
	users.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("group-admin");
	for (var i = 0; i != users.length; i++) {
		select.appendChild(renderPlatformUser(users[i]));
	}
	select.value = xowl.getLoggedInUserId();
}

function renderPlatformUser(user) {
	var image = document.createElement("img");
	image.src = "/web/assets/user.svg";
	image.width = 30
	image.height = 30
	image.style.marginRight = "20px";
	var option = document.createElement("option");
	option.value = user.identifier;
	option.appendChild(image);
	option.appendChild(document.createTextNode(user.name));
	return option;
}

function create() {
	if (!onOperationRequest("Creating new group ..."))
		return false;
	var identifier = document.getElementById("group-identifier").value;
	var name = document.getElementById("group-name").value;
	var admin = document.getElementById("group-admin").value;
	if (identifier == null || identifier == ""
		|| name == null || name == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.createPlatformGroup(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created group ", content, "."]});
			waitAndGo("group.html?id=" + encodeURIComponent(content.identifier));
		}
	}, identifier, name, admin);
	return false;
}
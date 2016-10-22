// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var BUSY = false;

function init() {
	setupPage(xowl);
	xowl.getPlatformUsers(function (status, ct, content) {
		if (status == 200) {
			renderPlatformUsers(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
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
	select.value = xowl.getUserId();
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
	var identifier = document.getElementById("group-identifier").value;
	var name = document.getElementById("group-name").value;
	var admin = document.getElementById("group-admin").value;
	if (identifier == null || identifier == ""
		|| name == null || name == "") {
		alert("All fields are mandatory.");
		return;
	}
	if (BUSY)
		return;
	BUSY = true;
	displayMessage("Creating group ...");
	xowl.createPlatformGroup(function (status, ct, content) {
		if (status == 200) {
			window.location.href = "group.html?id=" + encodeURIComponent(identifier);
			BUSY = false;
		} else {
			displayMessage(getErrorFor(status, content));
			BUSY = false;
		}
	}, identifier, name, admin);
}
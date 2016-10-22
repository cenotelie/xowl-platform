// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var DONE = 0;

function init() {
	setupPage(xowl);
	xowl.getPlatformUsers(function (status, ct, content) {
		if (status == 200) {
			renderPlatformUsers(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
	xowl.getPlatformGroups(function (status, ct, content) {
		if (status == 200) {
			renderPlatformGroups(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
	xowl.getPlatformRoles(function (status, ct, content) {
		if (status == 200) {
			renderPlatformRoles(content);
		} else {
			displayMessage(getErrorFor(status, content));
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
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function renderPlatformGroups(groups) {
	groups.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("groups");
	for (var  i = 0; i != groups.length; i++) {
		table.appendChild(renderPlatformGroup(groups[i]));
	}
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function renderPlatformRoles(roles) {
	roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != roles.length; i++) {
		table.appendChild(renderPlatformRole(roles[i]));
	}
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function renderPlatformUser(user) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/user.svg";
	image.width = 30
	image.height = 30
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(user.name));
	link.href="user.html?id=" + encodeURIComponent(user.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

function renderPlatformGroup(group) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "/web/assets/group.svg";
	image.width = 30
	image.height = 30
	image.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(group.name));
	link.href="group.html?id=" + encodeURIComponent(group.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
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
	return row;
}
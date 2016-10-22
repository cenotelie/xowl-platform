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

function getPlatformUsers(users) {
	users.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("users");
	for (var  i = 0; i != users.length; i++) {
		table.appendChild(getPlatformUser(users[i]));
	}
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function getPlatformGroups(groups) {
	groups.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("groups");
	for (var  i = 0; i != groups.length; i++) {
		table.appendChild(getPlatformGroup(groups[i]));
	}
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function getPlatformRoles(roles) {
	roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != roles.length; i++) {
		table.appendChild(getPlatformRole(roles[i]));
	}
	DONE++;
	if (DONE === 3)
		document.getElementById("loader").style.display = "none";
}

function getPlatformUser(user) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(user.name));
	link.href="user.html?id=" + encodeURIComponent(user.identifier);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

function getPlatformGroup(group) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(group.name));
	link.href="group.html?id=" + encodeURIComponent(group.identifier);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

function getPlatformRole(role) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href="role.html?id=" + encodeURIComponent(role.identifier);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}
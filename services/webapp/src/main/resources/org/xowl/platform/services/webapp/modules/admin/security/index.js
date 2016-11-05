// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security"}], function() {
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
		doGetGroups();
	});
}

function doGetGroups() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getPlatformGroups(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderPlatformGroups(content);
		}
		doGetRoles();
	});
}

function doGetRoles() {
	if (!onOperationRequest("Loading ..."))
		return;
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
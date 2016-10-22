// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var groupId = getParameterByName("id");
var oldName = null;

function init() {
	setupPage(xowl);
	if (!groupId || groupId === null || groupId === "")
		return;
	document.getElementById("placeholder-group").innerHTML = "Group " + groupId;
	displayMessage("Loading ...");
	xowl.getPlatformGroup(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, groupId);
}

function render(group) {
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
		table.appendChild(renderPlatformUser(group.admins[i]));
	}
	table = document.getElementById("members");
	for (var  i = 0; i != group.members.length; i++) {
		table.appendChild(renderPlatformUser(group.members[i]));
	}
	table = document.getElementById("roles");
	for (var  i = 0; i != group.roles.length; i++) {
		table.appendChild(renderPlatformRole(group.roles[i]));
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
	displayMessage("Renaming ...");
	xowl.renamePlatformGroup(function (status, ct, content) {
		if (status == 200) {
			displayMessage(null);
			oldName = null;
		} else {
			document.getElementById("group-name").value = oldName;
			displayMessage(getErrorFor(status, content));
			oldName = null;
		}
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
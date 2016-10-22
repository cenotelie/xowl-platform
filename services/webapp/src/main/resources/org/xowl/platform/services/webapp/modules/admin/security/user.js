// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var userId = getParameterByName("id");
var oldName = null;

function init() {
	setupPage(xowl);
	if (!userId || userId === null || userId === "")
		return;
	document.getElementById("placeholder-user").innerHTML = "User " + userId;
	displayMessage("Loading ...");
	xowl.getPlatformUser(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, userId);
}

function render(user) {
	document.getElementById("user-identifier").value = user.identifier;
	document.getElementById("user-name").value = user.name;
	user.roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != user.roles.length; i++) {
		table.appendChild(renderPlatformRole(user.roles[i]));
	}
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
	document.getElementById("user-name-edit").style.display = "none";
	document.getElementById("user-name-validate").style.display = "inline-block";
	document.getElementById("user-name-cancel").style.display = "inline-block";
	document.getElementById("user-name").readOnly = false;
	document.getElementById("user-name").focus();
	document.getElementById("user-name").select();
	oldName = document.getElementById("user-name").value;
}

function onClickValidate() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").readOnly = true;
	displayMessage("Renaming ...");
	xowl.renamePlatformUser(function (status, ct, content) {
		if (status == 200) {
			displayMessage(null);
			oldName = null;
		} else {
			document.getElementById("user-name").value = oldName;
			displayMessage(getErrorFor(status, content));
			oldName = null;
		}
	}, userId, document.getElementById("user-name").value);
}

function onClickCancel() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").value = oldName;
	document.getElementById("user-name").readOnly = true;
	oldName = null;
}
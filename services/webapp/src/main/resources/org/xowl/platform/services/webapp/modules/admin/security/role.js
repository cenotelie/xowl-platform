// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var roleId = getParameterByName("id");
var oldName = null;

function init() {
	setupPage(xowl);
	if (!roleId || roleId === null || roleId === "")
		return;
	document.getElementById("placeholder-role").innerHTML = "Role " + roleId;
	displayMessage("Loading ...");
	xowl.getPlatformRole(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, roleId);
}

function render(role) {
	document.getElementById("role-identifier").value = role.identifier;
	document.getElementById("role-name").value = role.name;
}

function onClickEdit() {
	if (oldName !== null)
		return;
	document.getElementById("role-name-edit").style.display = "none";
	document.getElementById("role-name-validate").style.display = "inline-block";
	document.getElementById("role-name-cancel").style.display = "inline-block";
	document.getElementById("role-name").readOnly = false;
	document.getElementById("role-name").focus();
	document.getElementById("role-name").select();
	oldName = document.getElementById("role-name").value;
}

function onClickValidate() {
	document.getElementById("role-name-edit").style.display = "inline-block";
	document.getElementById("role-name-validate").style.display = "none";
	document.getElementById("role-name-cancel").style.display = "none";
	document.getElementById("role-name").readOnly = true;
	displayMessage("Renaming ...");
	xowl.renamePlatformRole(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
			oldName = null;
		} else {
			document.getElementById("role-name").value = oldName;
			displayMessage(getErrorFor(status, content));
			oldName = null;
		}
	}, roleId, document.getElementById("role-name").value);
}

function onClickCancel() {
	document.getElementById("role-name-edit").style.display = "inline-block";
	document.getElementById("role-name-validate").style.display = "none";
	document.getElementById("role-name-cancel").style.display = "none";
	document.getElementById("role-name").value = oldName;
	document.getElementById("role-name").readOnly = true;
	oldName = null;
}
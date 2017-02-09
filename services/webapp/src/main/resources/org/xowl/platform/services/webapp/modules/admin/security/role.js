// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var roleId = getParameterByName("id");
var oldName = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Security", uri: ROOT + "/modules/admin/security/"},
			{name: "Role " + roleId}], function() {
		if (!roleId || roleId === null || roleId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformRole(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				render(content);
			}
		}, roleId);
	});
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
	if (!onOperationRequest("Renaming ..."))
		return;
	xowl.renamePlatformRole(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("role-name").value = oldName;
		}
		oldName = null;
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

function onClickDelete() {
	var result = confirm("Delete the role " + roleId + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting this role ..."))
		return;
	xowl.deletePlatformRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Deleted role " + roleId + ".");
			waitAndGo("index.html");
		}
	}, roleId);
}
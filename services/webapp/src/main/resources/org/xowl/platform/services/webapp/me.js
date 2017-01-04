// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var userId = getParameterByName("id");
var oldName = null;

function init() {
	doSetupPage(xowl, true, [{name: "My Account"}], function() {
		if (!userId || userId === null || userId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderUser(content);
			}
		}, userId);
	});
}

function renderUser(user) {
	document.getElementById("user-identifier").value = user.identifier;
	document.getElementById("user-name").value = user.name;
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
	if (!onOperationRequest("Renaming ..."))
		return;
	xowl.renamePlatformUser(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("user-name").value = oldName;
		}
		oldName = null;
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

function onClickChangePassword() {
	var result = confirm("Change password?");
	if (!result)
		return;
	var oldPassword = document.getElementById("user-old-password").value;
	var newPassword1 = document.getElementById("user-new-password1").value;
	var newPassword2 = document.getElementById("user-new-password2").value;
	if (newPassword1 !== newPassword2) {
		displayMessage("error", "The two passwords are different.");
		return;
	}
	if (!onOperationRequest("Changing password ..."))
		return;
	xowl.changePlatformUserPassword(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Password has been changed.");
			waitAndRefresh();
		}
	}, userId, oldPassword, newPassword);
}
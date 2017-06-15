// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var userId = xowl.getLoggedInUserId();
var user = null;
var oldName = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Security", uri: ROOT + "/modules/admin/security/"},
			{name: "My Account Security"}], function() {
		if (!userId || userId === null || userId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getPlatformUser(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				user = content;
				renderUser();
			}
		}, userId);
	});
}

function renderUser() {
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
	image.src = ROOT + "/assets/role.svg";
	image.width = 30
	image.height = 30
	image.style.marginRight = "20px";
	image.title = role.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href="role.html?id=" + encodeURIComponent(role.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

function onClickNameEdit() {
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

function onClickNameValidate() {
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

function onClickNameCancel() {
	document.getElementById("user-name-edit").style.display = "inline-block";
	document.getElementById("user-name-validate").style.display = "none";
	document.getElementById("user-name-cancel").style.display = "none";
	document.getElementById("user-name").value = oldName;
	document.getElementById("user-name").readOnly = true;
	oldName = null;
}

function onPopupChangePasswordOpen() {
	showPopup("popup-change-password");
}

function onPopupChangePasswordOk() {
	if (!onOperationRequest("Changing password ..."))
		return;
	var oldPassword = document.getElementById("user-old-password").value;
	var newPassword1 = document.getElementById("user-new-password1").value;
	var newPassword2 = document.getElementById("user-new-password2").value;
	if (oldPassword == null || oldPassword == "" || newPassword1 == null || newPassword1 == "" || newPassword2 == null || newPassword2 == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	if (newPassword1 !== newPassword2) {
		onOperationAbort("The two passwords are different.");
		return false;
	}
	hidePopup("popup-change-password");
	xowl.changePlatformUserPassword(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Password has been reset.");
			waitAndRefresh();
		}
	}, userId, oldPassword, newPassword2);
}

function onPopupChangePasswordCancel() {
	hidePopup("popup-change-password");
	document.getElementById("user-old-password").value = "";
	document.getElementById("user-new-password1").value = "";
	document.getElementById("user-new-password2").value = "";
}
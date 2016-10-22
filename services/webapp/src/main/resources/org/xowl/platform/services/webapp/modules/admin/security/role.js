// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var roleId = getParameterByName("id");

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
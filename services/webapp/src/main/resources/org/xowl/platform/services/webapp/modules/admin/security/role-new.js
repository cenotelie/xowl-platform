// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var BUSY = false;

function init() {
	setupPage(xowl);
	displayMessage(null);
}

function create() {
	var identifier = document.getElementById("role-identifier").value;
	var name = document.getElementById("role-name").value;
	if (identifier == null || identifier == ""
		|| name == null || name == "") {
		alert("All fields are mandatory.");
		return;
	}
	if (BUSY)
		return;
	BUSY = true;
	displayMessage("Creating role ...");
	xowl.createPlatformRole(function (status, ct, content) {
		if (status == 200) {
			window.location.href = "role.html?id=" + encodeURIComponent(identifier);
			BUSY = false;
		} else {
			displayMessage(getErrorFor(status, content));
			BUSY = false;
		}
	}, identifier, name);
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var BUSY = false;

function init() {
	setupPage(xowl);
	displayMessage(null);
}

function create() {
	var identifier = document.getElementById("user-identifier").value;
	var name = document.getElementById("user-name").value;
	var password1 = document.getElementById("user-password1").value;
	var password2 = document.getElementById("user-password2").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| password1 == null || password1 == ""
		|| password2 == null || password2 == "") {
		alert("All fields are mandatory.");
		return;
	}
	if (password1 !== password2) {
		alert("The two passwords are different.");
		return;
	}
	if (BUSY)
		return;
	BUSY = true;
	xowl.createPlatformUser(function (status, ct, content) {
		if (status == 200) {
			window.location.href = "user.html?id=" + encodeURIComponent(identifier);
			BUSY = false;
		} else {
			displayMessage(getErrorFor(status, content));
			BUSY = false;
		}
	}, identifier, name, password1);
}
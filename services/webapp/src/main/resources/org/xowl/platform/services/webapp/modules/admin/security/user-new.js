// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "New User"}], function() {
	});
}

function create() {
	if (!onOperationRequest("Creating new user ..."))
		return false;
	var identifier = document.getElementById("user-identifier").value;
	var name = document.getElementById("user-name").value;
	var password1 = document.getElementById("user-password1").value;
	var password2 = document.getElementById("user-password2").value;
	if (identifier == null || identifier == ""
		|| name == null || name == ""
		|| password1 == null || password1 == ""
		|| password2 == null || password2 == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	if (password1 !== password2) {
		onOperationAbort("The two passwords are different.");
		return false;
	}
	xowl.createPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			window.location.href = "user.html?id=" + encodeURIComponent(identifier);
		}
	}, identifier, name, password1);
	return false;
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, false, [{name: "Login"}], function() {});
}

function onLoginButton() {
	if (!onOperationRequest("Trying to login ..."))
		return false;
	var login = document.getElementById("field-login").value;
	var password = document.getElementById("field-password").value;
	if (login === null || login === "" || password === null || password === "") {
		onOperationAbort("Login / Password is empty!");
		return false;
	}
	xowl.login(function (status, type, content) {
		if (onOperationEnded(status, content, "Failed to login, verify your login and password.")) {
			window.location.href = "index.html";
		}
	}, login, password);
	return false;
}
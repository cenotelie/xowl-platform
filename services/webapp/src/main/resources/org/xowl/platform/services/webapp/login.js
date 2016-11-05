// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();
var BUSY = false;

function init() {
	doSetupPage(xowl, false, [{name: "Login"}], function() {});
}

function onLoginButton() {
	if (BUSY) {
		displayMessage("error", "Another operation is going on ...");
		return;
	}
	var login = document.getElementById("field-login").value;
	var password = document.getElementById("field-password").value;
	if (login === null || login === "" || password === null || password === "")
		return;
	BUSY = true;
	var remover = displayLoader("Trying to login ...");
	xowl.login(function (code, type, content) {
		SENT = false;
		if (code === 200) {
			window.location.href = "index.html";
		} else {
			remover();
			displayMessage("error", "Failed to login, verify your login and password.");
		}
	}, login, password);
	return false;
}
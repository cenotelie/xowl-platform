// Copyright (c) 2015 Laurent Wouters
// Provided under LGPL v3

var xowl = new XOWL();
var SENT = false;

function init() {
	document.getElementById("branding-title").onload = function () {
		document.title = document.getElementById("branding-title").contentDocument.getElementById("title-value").innerHTML + document.title;
	};
	displayMessage(null);
}

function onLoginButton() {
	if (SENT)
		return false;
	SENT = true;
	var login = document.getElementById("field-login").value;
	var password = document.getElementById("field-password").value;
	if (login === null || login === "" || password === null || password === "")
		return;
	displayMessage("Trying to login ...");
	xowl.login(function (code, type, content) {
		SENT = false;
		if (code === 200) {
			window.location.href = "index.html";
		} else {
			displayMessage("Failed to login, verify your login and password.");
		}
	}, login, password);
	return false;
}
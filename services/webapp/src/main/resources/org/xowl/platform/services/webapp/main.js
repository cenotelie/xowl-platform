// Copyright (c) 2015 Laurent Wouters
// Provided under LGPL v3

var xowl = new XOWL();
var FLAG = false;

function init() {
	if (!xowl.isLoggedIn()) {
		document.location.href = "index.html";
		return;
	}
	document.getElementById("branding-title").onload = function () {
		document.title = document.getElementById("branding-title").contentDocument.getElementById("title-value").innerHTML + document.title;
	};
	document.getElementById("btn-logout").innerHTML = "Logout (" + xowl.getUser() + ")";
}

function onButtonLogout() {
	xowl.logout();
	document.location.href = "index.html";
}
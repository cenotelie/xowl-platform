// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var FAMILY = getParameterByName("id");

function init() {
	setupPage(xowl);
	if (FAMILY === null || FAMILY == "")
		return;
}

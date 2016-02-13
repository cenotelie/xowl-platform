// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var FAMILY = getParameterByName("id");

function init() {
	setupPage(xowl);
	if (FAMILY === null || FAMILY == "")
		return;
}

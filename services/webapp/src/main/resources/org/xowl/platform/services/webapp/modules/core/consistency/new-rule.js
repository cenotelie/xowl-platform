// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	displayMessage(null);
	var prefixes = "";
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		prefixes += "PREFIX " + DEFAULT_URI_MAPPINGS[i][0] + ": <" + DEFAULT_URI_MAPPINGS[i][1] + ">\n";
	}
	prefixes += "PREFIX kernel: <http://xowl.org/platform/schemas/kernel#>\n";
	prefixes += "PREFIX consistency: <http://xowl.org/platform/schemas/consistency#>\n";
	document.getElementById("input-prefixes").value = prefixes;
}

function onClickNewRule() {
	var name = document.getElementById("input-name").value;
	var message = document.getElementById("input-message").value;
	var prefixes = document.getElementById("input-prefixes").value;
	var conditions = document.getElementById("input-conditions").value;
	if (name === null || name === "" || message === null || message === "" || conditions === null || conditions === "")
		return;
	displayMessage("Creating new rule ...")
	xowl.newConsistencyRule(function (status, ct, content) {
		if (status == 200) {
			window.location.href = "rules.html";
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, name, message, prefixes, conditions);
}
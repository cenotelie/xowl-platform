// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "New Rule"}], function() {});
	var prefixes = "";
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		prefixes += "PREFIX " + DEFAULT_URI_MAPPINGS[i][0] + ": <" + DEFAULT_URI_MAPPINGS[i][1] + ">\n";
	}
	prefixes += "PREFIX kernel: <http://xowl.org/platform/schemas/kernel#>\n";
	prefixes += "PREFIX consistency: <http://xowl.org/platform/schemas/consistency#>\n";
	document.getElementById("input-prefixes").value = prefixes;
}

function onClickNewRule() {
	if (!onOperationRequest("Creating new rule ..."))
		return false;
	var name = document.getElementById("input-name").value;
	var message = document.getElementById("input-message").value;
	var prefixes = document.getElementById("input-prefixes").value;
	var conditions = document.getElementById("input-conditions").value;
	if (name === null || name === "" || message === null || message === "" || conditions === null || conditions === "") {
		onOperationAbort("All fields are mandatory.");
		return;
	}
	xowl.newConsistencyRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created rule ", content, "."]});
			waitAndGo("rules.html");
		}
	}, name, message, prefixes, conditions);
}
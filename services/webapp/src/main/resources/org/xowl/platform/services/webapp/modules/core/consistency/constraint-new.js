// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Manage Consistency", uri: ROOT + "/modules/core/consistency/"},
			{name: "Constraints", uri: ROOT + "/modules/core/consistency/constraints.html"},
			{name: "New Constraint"}], function() {});
	var prefixes = "";
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		prefixes += "PREFIX " + DEFAULT_URI_MAPPINGS[i][0] + ": <" + DEFAULT_URI_MAPPINGS[i][1] + ">\n";
	}
	prefixes += "PREFIX kernel: <http://xowl.org/platform/schemas/kernel#>\n";
	prefixes += "PREFIX consistency: <http://xowl.org/platform/schemas/consistency#>\n";
	document.getElementById("input-prefixes").value = prefixes;
}

function onClickNewRule() {
	if (!onOperationRequest("Creating new constraint ..."))
		return false;
	var name = document.getElementById("input-name").value;
	var message = document.getElementById("input-message").value;
	var prefixes = document.getElementById("input-prefixes").value;
	var antecedents = document.getElementById("input-antecedents").value;
	var guard = document.getElementById("input-guard").value;
	if (guard === null)
		guard = "";
	if (name === null || name === "" || message === null || message === "" || antecedents === null || antecedents === "") {
		onOperationAbort("All fields are mandatory.");
		return;
	}
	xowl.newConsistencyConstraint(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "fr.cenotelie.commons.utils.RichString", parts: ["Created constraint ", content, "."]});
			waitAndGo("constraints.html");
		}
	}, name, message, prefixes, antecedents, guard);
}
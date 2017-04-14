// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var ruleId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "Constraints", uri: ROOT + "/modules/core/consistency/constraints.html"},
			{name: "Constraint " + ruleId}], function() {
		if (!ruleId || ruleId === null || ruleId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getConsistencyConstraint(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				render(content);
			}
		}, ruleId);
	});
}

function render(rule) {
	document.getElementById("rule-id").value = rule.identifier;
	document.getElementById("rule-name").value = rule.name;
	document.getElementById("rule-active").value = rule.isActive ? "YES" : "NO";
	document.getElementById("rule-def").value = rule.definition;
	document.getElementById("btn-download").href = "data:" + MIME_JSON + ";base64," + btoa(JSON.stringify(rule));
	document.getElementById("btn-download").download = rule.name + ".json";
}
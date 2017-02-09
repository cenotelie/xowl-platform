// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var ruleId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "Consistency Rule " + ruleId}], function() {
		if (!ruleId || ruleId === null || ruleId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getConsistencyRule(function (status, ct, content) {
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
}
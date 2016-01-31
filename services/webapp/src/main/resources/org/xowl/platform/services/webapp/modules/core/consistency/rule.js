// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ruleId = getParameterByName("id");

function init() {
	setupPage(xowl);
	if (!ruleId || ruleId === null || ruleId === "")
		return;
	document.getElementById("placeholder-rule").innerHTML = ruleId;
	displayMessage("Loading ...");
	xowl.getConsistencyRule(function (status, ct, content) {
		if (status == 200) {
			render(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, ruleId);
}

function render(rule) {
	document.getElementById("rule-id").value = rule.id;
	document.getElementById("rule-name").value = rule.name;
	document.getElementById("rule-active").value = rule.isActive ? "YES" : "NO";
	document.getElementById("rule-def").value = rule.definition;
}
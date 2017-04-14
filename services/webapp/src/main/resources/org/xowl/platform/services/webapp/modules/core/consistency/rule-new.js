// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "Reasoning Rule", uri: ROOT + "/modules/core/consistency/rules.html"},
			{name: "New Rule"}], function() {});
	var content = "BASE <http://xowl.org/platform/schemas/consistency>\n";
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		content += "PREFIX " + DEFAULT_URI_MAPPINGS[i][0] + ": <" + DEFAULT_URI_MAPPINGS[i][1] + ">\n";
	}
	content += "PREFIX kernel: <http://xowl.org/platform/schemas/kernel#>\n";
	content += "PREFIX consistency: <http://xowl.org/platform/schemas/consistency#>\n";
	content += "\n";
	content += "rule kernel:transitiveMyProperty {\n";
	content += "    SELECT DISTINCT ?x ?z WHERE {\n";
	content += "        GRAPH ?g1 { ?x kernel:myProperty ?y }\n";
	content += "        GRAPH ?g2 { ?y kernel:myProperty ?z }\n";
	content += "    }\n";
	content += "} => {\n";
	content += "    ?x kernel:myProperty ?z\n";
	content += "}\n";
	document.getElementById("input-definition").value = content;
}

function onClickNewRule() {
	if (!onOperationRequest("Creating new reasoning rule ..."))
		return false;
	var name = document.getElementById("input-name").value;
	var definition = document.getElementById("input-definition").value;
	if (name === null || name === "" || definition === null || definition === "") {
		onOperationAbort("All fields are mandatory.");
		return;
	}
	xowl.newReasoningRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created reasoning rule ", content, "."]});
			waitAndGo("rules.html");
		}
	}, name, definition);
}
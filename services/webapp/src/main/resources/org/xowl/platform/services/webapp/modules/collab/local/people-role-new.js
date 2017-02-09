// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Local Collaboration", uri: ROOT + "/modules/collab/local/"},
			{name: "People and Roles", uri: ROOT + "/modules/collab/local/people.html"},
			{name: "New Role ..."}], function() {
	});
}

function create() {
	if (!onOperationRequest("Creating new role ..."))
		return false;
	var identifier = document.getElementById("role-identifier").value;
	var name = document.getElementById("role-name").value;
	if (identifier == null || identifier == ""
		|| name == null || name == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}
	xowl.createCollaborationRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created role ", content, "."]});
			waitAndGo("people.html");
		}
	}, {identifier: identifier, name: name});
	return false;
}
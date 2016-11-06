// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
			{name: "New Role"}], function() {
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
	xowl.createPlatformRole(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.platform.kernel.RichString", parts: ["Created role ", content, "."]});
			waitAndGo("role.html?id=" + encodeURIComponent(content.identifier));
		}
	}, identifier, name);
	return false;
}
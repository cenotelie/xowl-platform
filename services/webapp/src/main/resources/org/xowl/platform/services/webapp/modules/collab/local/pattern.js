// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Local Collaboration", uri: "/web/modules/collab/local/"},
			{name: "Collaboration Pattern"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationPattern(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			document.getElementById("pattern-identifier").value = content.identifier;
			document.getElementById("pattern-name").value = content.name;
		}
	});
}

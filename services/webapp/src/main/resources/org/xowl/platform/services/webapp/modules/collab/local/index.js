// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Local Collaboration"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationManifest(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			document.getElementById("collaboration-identifier").value = content.identifier;
			document.getElementById("collaboration-name").value = content.name;
		}
	});
}

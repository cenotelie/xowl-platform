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

function onClickArchive() {
	var result = confirm("Archive this collaboration? (This will stop this platform.)");
	if (!result)
		return;
	if (!onOperationRequest("Archiving this collaboration ..."))
		return;
	xowl.archiveCollaboration(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "The collaboration has been archived.");
			waitAndRefresh();
		}
	});
	return false;
}

function onClickDelete() {
	var result = confirm("Delete this collaboration and all its data? (This will stop this platform.)");
	if (!result)
		return;
	if (!onOperationRequest("Deleting this collaboration ..."))
		return;
	xowl.deleteCollaboration(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "The collaboration has been deleted.");
			waitAndRefresh();
		}
	});
	return false;
}
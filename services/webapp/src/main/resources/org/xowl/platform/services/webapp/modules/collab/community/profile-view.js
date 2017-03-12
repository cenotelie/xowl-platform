// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var profileId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community", uri: ROOT + "/modules/collab/community/"},
			{name: "Profile " + profileId}], function() {
		if (!profileId || profileId === null || profileId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content, null)) {
		}
	}, profileId);
	xowl.getPublicProfile(function (status, ct, content) {
		if (onOperationEnded(status, content, null)) {
		}
	}, profileId);
}
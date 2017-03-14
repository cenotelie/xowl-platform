// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var badgeId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community", uri: ROOT + "/modules/collab/community/"},
			{name: "Badges", uri: ROOT + "/modules/collab/community/badges.html"},
			{name: "Badge " + badgeId}], function() {
		if (!badgeId || badgeId === null || badgeId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getBadge(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderBadge(content);
		}
	}, badgeId);
}

function renderBadge(badge) {
	document.getElementById("badge-identifier").value = badge.identifier;
	document.getElementById("badge-name").value = badge.name;
	document.getElementById("badge-description").value = badge.description;
	document.getElementById("image").src = "data:" + badge.imageMime + ";base64," + badge.imageContent;
}
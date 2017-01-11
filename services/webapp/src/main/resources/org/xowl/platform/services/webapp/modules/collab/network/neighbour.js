// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var collabId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Collaborations Network", uri: "/web/modules/collab/network/"},
			{name: "Neighbour " + collabId}], function() {
		if (!collabId || collabId === null || collabId === "")
			return;
	});
}
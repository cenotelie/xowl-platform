// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var platformRoles = null;
var platformArchetypes = null;
var platformSpecifications = null;

var specification = {
	name: "",
	inputs: [],
	outputs: [],
	roles: [],
	pattern: {}
};

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Collaborations Network", uri: "/web/modules/collab/network/"},
			{name: "New Collaboration"}], function() {

	});
}


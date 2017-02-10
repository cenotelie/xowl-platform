// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	var url = document.location.href;
	var index = url.lastIndexOf("/");
	var code = url.substring(index, url.length - 5);
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Documentation", uri: ROOT + "/modules/admin/documentation/"},
			{name: "Errors", uri: ROOT + "/modules/admin/documentation/errors.html"},
			{name: "Error " + code}], function() {
	});
}

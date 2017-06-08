// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var addonId = getParameterByName("id");
var addon = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Management", uri: ROOT + "/modules/admin/platform/"},
			{name: "Platform Addons", uri: ROOT + "/modules/admin/platform/addons.html"},
			{name: addonId}], function() {
		if (!addonId || addonId === null || addonId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getPlatformAddon(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			addon = content;
			renderAddon();
		}
	}, addonId);
}

function renderAddon() {
	document.getElementById("field-identifier").value = addon.identifier;
	document.getElementById("field-name").value = addon.name;
	document.getElementById("field-description").value = addon.description;
	document.getElementById("field-version-number").value = addon.version.number;
	document.getElementById("field-version-scm-tag").value = addon.version.scmTag;
	document.getElementById("field-version-build-user").value = addon.version.buildUser;
	document.getElementById("field-version-build-tag").value = addon.version.buildTag;
	document.getElementById("field-version-build-timestamp").value = addon.version.buildTimestamp;
	document.getElementById("field-copyright").value = addon.copyright;
	document.getElementById("field-vendor").value = addon.vendor;
	document.getElementById("field-pricing").value = addon.pricing;
	document.getElementById("field-tags").value = addon.tags;
	document.getElementById("field-license-name").value = addon.license.name;
	document.getElementById("field-license-text").value = addon.license.fullText;
}

function onClickUninstall() {
	popupConfirm(richString(["Uninstall addon ", addon, "?"]), function() {
		if (!onOperationRequest(richString(["Uninstalling addon ", addon, " ..."])))
			return;
		xowl.uninstallPlatformAddon(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Uninstalled addon ",addon, ", restart the platform to complete the update."]));
				waitAndGo("addons.html");
			}
		}, addonId);
	});
}

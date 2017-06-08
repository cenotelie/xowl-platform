// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var addonId = getParameterByName("id");
var addon = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Management", uri: ROOT + "/modules/admin/platform/"},
			{name: "Platform Marketplace", uri: ROOT + "/modules/admin/platform/marketplace.html"},
			{name: addonId}], function() {
		if (!addonId || addonId === null || addonId === "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.marketplaceGetAddon(function (status, ct, content) {
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

function onClickInstall() {
	popupConfirm("Platform Management", richString(["Install addon ", addon, "?"]), function() {
		if (!onOperationRequest(richString(["Launching installation of ", addon, " ..."])))
			return;
		xowl.marketplaceInstallAddon(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Launched job ", content]));
				waitForJob(content.identifier, content.name, function (job) {
					onInstallJobComplete(job.result);
				});
			}
		}, addonId);
	});
}

function onInstallJobComplete(xsp) {
	if (!xsp.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!xsp.isSuccess) {
		displayMessage("error", "FAILURE: " + xsp.message);
	} else {
		displayMessage("success", richString([
			"Installed addon ",
			xsp.payload,
			", restart the platform to complete the update."]));
		waitAndGo("marketplace.html");
	}
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Management", uri: "/web/modules/admin/platform/"},
			{name: "Platform Addons"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getPlatformAddons(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderAddons(content);
		}
	});
}

function renderAddons(addons) {
	addons.sort(function (x, y) {
		return x.identifier.localeCompare(y.name);
	});
	var table = document.getElementById("addons");
	for (var i = 0; i != addons.length; i++) {
		table.appendChild(renderAddon(addons[i]));
	}
}

function renderAddon(addon) {
	var panel = document.createElement("div");
	panel.classList.add("panel");
	panel.classList.add("panel-default");
	var panelBody = document.createElement("div");
	panelBody.classList.add("panel-body");
	var row = document.createElement("div");
	row.classList.add("row");
	var cell1 = document.createElement("div");
	cell1.classList.add("col-md-4");
	var cell2 = document.createElement("div");
	cell2.classList.add("col-md-8");
	var image = document.createElement("img");
	image.width = "100";
	image.height = "100";
	image.title = addon.identifier;
	if (addon.iconContent !== "") {
		if (addon.iconName.endsWith(".png"))
			image.src="data:image/png;base64," + addon.iconContent;
		else if (addon.iconName.endsWith(".gif"))
			image.src="data:image/gif;base64," + addon.iconContent;
		else if (addon.iconName.endsWith(".svg"))
			image.src="data:image/svg+xml;base64," + addon.iconContent;
	}
	var titleLink = document.createElement("a");
	titleLink.href = "addon.html?id=" + encodeURIComponent(addon.identifier);
	titleLink.appendChild(document.createTextNode(addon.name + " - v" + addon.version.number));
	var title = document.createElement("h4");
	var contentDescription = document.createElement("p");
	contentDescription.appendChild(document.createTextNode(addon.description));
	var contentTags = document.createElement("p");
	for (var i = 0; i != addon.tags.length; i++) {
		var badge = document.createElement("span");
		badge.className = "badge";
		badge.appendChild(document.createTextNode(addon.tags[i]));
		contentTags.appendChild(badge);
	}
	var contentPricing = document.createElement("p");
	contentPricing.appendChild(document.createTextNode(addon.pricing));
	var contentVendor = document.createElement("p");
	contentVendor.appendChild(document.createTextNode("Provided by "));
	var vendorLink = document.createElement("a");
	vendorLink.href = addon.vendorLink;
	vendorLink.appendChild(document.createTextNode(addon.vendor));

	contentVendor.appendChild(vendorLink);
	title.appendChild(titleLink);
	cell2.appendChild(title);
	cell2.appendChild(contentDescription);
	cell2.appendChild(contentTags);
	cell2.appendChild(contentPricing);
	cell2.appendChild(contentVendor);
	cell1.appendChild(image);
	row.appendChild(cell1);
	row.appendChild(cell2);
	panelBody.appendChild(row);
	panel.appendChild(panelBody);
	return panel;
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Management"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getPlatformProduct(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderProduct(content);
		}
	});
	xowl.getPlatformBundles(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderBundles(content);
		}
	});
}

function renderProduct(product) {
	document.getElementById("field-identifier").value = product.identifier;
	document.getElementById("field-name").value = product.name;
	document.getElementById("field-version-number").value = product.version.number;
	document.getElementById("field-version-scm-tag").value = product.version.scmTag;
	document.getElementById("field-version-build-user").value = product.version.buildUser;
	document.getElementById("field-version-build-tag").value = product.version.buildTag;
	document.getElementById("field-version-build-timestamp").value = product.version.buildTimestamp;
	document.getElementById("field-copyright").value = product.copyright;
	document.getElementById("field-vendor").value = product.vendor;
	document.getElementById("field-license-name").value = product.license.name;
	document.getElementById("field-license-text").value = product.license.fullText;
}


function renderBundles(bundles) {
	bundles.sort(function (x, y) {
		return x.identifier.localeCompare(y.identifier);
	});
	var table = document.getElementById("bundles");
	for (var i = 0; i != bundles.length; i++) {
		var cells = [
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td")
		];
		cells[0].appendChild(document.createTextNode(bundles[i].identifier));
		cells[1].appendChild(document.createTextNode(bundles[i].vendor));
		cells[2].appendChild(document.createTextNode(bundles[i].name));
		cells[3].appendChild(document.createTextNode(bundles[i].version));
		cells[4].appendChild(document.createTextNode(bundles[i].state));
		var row = document.createElement("tr");
		row.appendChild(cells[0]);
		row.appendChild(cells[1]);
		row.appendChild(cells[2]);
		row.appendChild(cells[3]);
		row.appendChild(cells[4]);
		table.appendChild(row);
	}
}

function onClickShutdown() {
	var result = confirm("Shutdown the platform?");
	if (!result)
		return;
	if (!onOperationRequest("Shutting down the platform ..."))
		return;
	xowl.platformShutdown(function (status, ct, content) {
		if (status == 0) {
			displayMessage("success", "The platform shut down.");
		} else if (onOperationEnded(status, content)) {
			displayMessage("success", "The platform shut down.");
		}
	});
}

function onClickRestart() {
	var result = confirm("Restart the platform?");
	if (!result)
		return;
	if (!onOperationRequest("Restarting down the platform ..."))
		return;
	xowl.platformRestart(function (status, ct, content) {
		if (status == 0) {
			displayMessage("success", "The platform is restarting.");
		} else if (onOperationEnded(status, content)) {
			displayMessage("success", "The platform is restarting.");
		}
	});
}

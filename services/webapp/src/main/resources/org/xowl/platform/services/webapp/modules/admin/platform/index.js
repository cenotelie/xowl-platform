// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var BUSY = false;

function init() {
	doSetupPage(xowl, true, [
			{name: "Administration Module", uri: "/web/modules/admin/"},
			{name: "Platform Management"}], function() {
		var remover = displayLoader("Loading ...");
		xowl.getPlatformBundles(function (status, ct, content) {
			if (status == 200) {
				renderBundles(content);
				remover();
			} else {
				remover();
				displayMessageHttpError(status, content);
			}
		});
	});
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
	if (BUSY) {
		displayMessage("error", "Another operation is going on ...");
		return;
	}
	var result = confirm("Shutdown the platform?");
	if (result == true) {
		BUSY = true;
		var remover = displayLoader("Shutting down the platform ...");
		xowl.platformShutdown(function (status, ct, content) {
			BUSY = false;
			if (status == 200) {
				remover();
				displayMessage("success", "Platform shut down.");
			} else {
				remover();
				displayMessageHttpError(status, content);
			}
		});
	}
}

function onClickRestart() {
	if (BUSY) {
		displayMessage("error", "Another operation is going on ...");
		return;
	}
	var result = confirm("Restart the platform?");
	if (result == true) {
		BUSY = true;
		var remover = displayLoader("Restarting the platform ...");
		xowl.platformRestart(function (status, ct, content) {
			BUSY = false;
			if (status == 200) {
				remover();
				displayMessage("success", "Platform si restarting.");
			} else {
				remover();
				displayMessageHttpError(status, content);
			}
		});
	}
}

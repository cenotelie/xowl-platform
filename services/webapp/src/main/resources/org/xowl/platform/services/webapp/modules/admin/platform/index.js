// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	document.getElementById("certificate-cn").value = window.location.hostname;
	xowl.getPlatformBundles(function (status, ct, content) {
		if (status == 200) {
			renderBundles(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
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
	var result = confirm("Shutdown the platform?");
	if (result == true) {
		displayMessage("Shutting down the platform ...");
		xowl.platformShutdown(function (status, ct, content) {
			if (status == 200) {
				document.location.href = "/web/";
			} else {
				displayMessage(getErrorFor(status, content));
			}
		});
	}
}

function onClickRestart() {
	var result = confirm("Restart the platform?");
	if (result == true) {
		displayMessage("Restarting the platform ...");
		xowl.platformRestart(function (status, ct, content) {
			if (status == 200) {
				document.location.href = "/web/";
			} else {
				displayMessage(getErrorFor(status, content));
			}
		});
	}
}

function onClickRegenerateTLS() {
	var alias = document.getElementById("certificate-cn").value;
	if (alias === null || alias === "")
		return;
	var result = confirm("Regenerate the TLS certificate?");
	if (result == true) {
		displayMessage("Regenerating TLS certificate ...");
		xowl.platformRegenerateTLS(function (status, ct, content) {
			if (status == 200) {
				displayMessage("TLS certificate has been regenerated, restart to update.");
			} else {
				displayMessage(getErrorFor(status, content));
			}
		}, alias);
	}
}
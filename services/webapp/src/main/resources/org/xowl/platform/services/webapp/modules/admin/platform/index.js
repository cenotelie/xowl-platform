// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
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
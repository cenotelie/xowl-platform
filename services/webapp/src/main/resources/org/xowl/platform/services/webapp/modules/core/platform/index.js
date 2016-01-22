// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
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
	var table = document.getElementById("bundles");
	for (var i = 0; i != bundles.length; i++) {
		var cells = [
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td")
		];
		cells[0].appendChild(document.createTextNode(bundles[i].id));
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
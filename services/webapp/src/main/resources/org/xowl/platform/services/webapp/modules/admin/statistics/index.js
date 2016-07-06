// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	xowl.getDatabasesStatistics(function (status, ct, content) {
		if (status == 200) {
			renderDatabasesStats(content);
			document.getElementById("loader").style.display = "none";
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderDatabasesStats(stats) {
	renderDatabaseStat("longTerm", stats.longTerm);
	renderDatabaseStat("live", stats.live);
	renderDatabaseStat("service", stats.service);
}

function renderDatabaseStat(dbName, stats) {
	renderFiles(dbName, stats.files);
}

function renderFiles(dbName, stats) {
	for (var  i = 0; i != stats.length; i++) {
		document.getElementById("files").appendChild(renderFile(stats[i]));
	}
}

function renderFile(stats) {
	var row = document.createElement("tr");
	var cells = [ document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")];
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(stats.fileName));
	link.href="dbFile.html?file=" + encodeURIComponent(stats.fileName);
	cells[0].appendChild(link);
	cells[1].appendChild(document.createTextNode(stats.accessesPerSecond));
	cells[2].appendChild(document.createTextNode(stats.accessesContention));
	cells[3].appendChild(document.createTextNode(stats.loadedBlocks));
	cells[4].appendChild(document.createTextNode(stats.dirtyBlocks));
	row.appendChild(cells[0]);
	row.appendChild(cells[1]);
	row.appendChild(cells[2]);
	row.appendChild(cells[3]);
	row.appendChild(cells[4]);
	return row;
}

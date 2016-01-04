// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTS_ALL = null;
var ARTIFACTS_LIVE = null;
var ARTIFACTS = null;
var CONNECTORS = null;

function init() {
	xowl.getConnectors(function (status, ct, content) {
		if (status == 200) {
			CONNECTORS = content;
			renderConnectors();
		}
	});
	xowl.getAllArtifacts(function (status, ct, content) {
		if (status == 200) {
			ARTIFACTS_ALL = content;
			prepareArtifacts();
		}
	});
	xowl.getLiveArtifacts(function (status, ct, content) {
		if (status == 200) {
			ARTIFACTS_LIVE = content;
			prepareArtifacts();
		}
	});
}

function prepareArtifacts() {
	if (ARTIFACTS_ALL == null || ARTIFACTS_LIVE == null)
		return;
	ARTIFACTS = [];
	for (var i = 0; i != ARTIFACTS_ALL.length; i++) {
		var isLive = false;
		for (var j = 0; j != ARTIFACTS_LIVE.length; j++) {
			if (ARTIFACTS_LIVE[j].identifier === ARTIFACTS_ALL[i].identifier) {
				isLive = true;
				break;
			}
		}
		ARTIFACTS_ALL[i].isLive = isLive;
		if (ARTIFACTS_ALL[i].hasOwnProperty("base") && ARTIFACTS_ALL[i].base != "") {
			var base = ARTIFACTS_ALL[i].base;
			var found = false;
			for (var j = 0; j != ARTIFACTS.length; j++) {
				if (ARTIFACTS[j].base === base) {
					ARTIFACTS[j].artifacts.push(ARTIFACTS_ALL[i]);
					if (isLive)
						ARTIFACTS[j].live.push(ARTIFACTS_ALL[i]);
					found = true;
					break;
				}
			}
			if (!found) {
				ARTIFACTS.push({
					name: ARTIFACTS_ALL[i].name,
					base: base,
					version: ARTIFACTS_ALL[i].version,
					artifacts: [ARTIFACTS_ALL[i]],
					live: (isLive ? [ARTIFACTS_ALL[i]] : [])
				});
			}
		} else {
			ARTIFACTS.push({
				name: ARTIFACTS_ALL[i].name,
				base: "",
				version: ARTIFACTS_ALL[i].version,
				artifacts: [ARTIFACTS_ALL[i]]
			});
		}
	}
	for (var i = 0; i != ARTIFACTS.length; i++) {
		if (ARTIFACTS[i].artifacts.length === 1)
			continue;
		ARTIFACTS[i].artifacts.sort(function (x, y) {
			return y.version.localeCompare(x.version);
		});
		ARTIFACTS[i].name = ARTIFACTS[i].artifacts[0].name;
		ARTIFACTS[i].version = ARTIFACTS[i].artifacts[0].version;
	}
	ARTIFACTS.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	renderArtifacts();
}

function renderConnectors() {
	var rows = document.getElementById("connectors");
	for (var i = 0; i != CONNECTORS.length; i++) {
		var connector = CONNECTORS[i];
		if (CONNECTORS[i].canPullInput) {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var img = document.createElement("img");
			img.src = "../assets/connector.svg";
			img.width = 32;
			img.height = 32;
			var link = document.createElement("a");
			link.href = "connectors.html?id=" + encodeURIComponent(connector.identifier);
			link.appendChild(img);
			link.appendChild(document.createTextNode(connector.name));
			cell1.appendChild(link);
			var button = document.createElement("button");
			button.type = "button";
			button.className = "btn btn-primary";
			button.appendChild(document.createTextNode("Pull from client"));
			button.onclick = function () { onClickPull(connector); }
			cell2.appendChild(button);
			row.appendChild(cell1);
			row.appendChild(cell2);
			rows.appendChild(row);
		} else if (CONNECTORS[i].queue.length > 0) {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var img = document.createElement("img");
			img.src = "../assets/connector.svg";
			img.width = 32;
			img.height = 32;
			var badge = document.createElement("span");
			badge.className = "badge";
			badge.appendChild(document.createTextNode(connector.queue.length.toString()));
			var link = document.createElement("a");
			link.href = "connectors.html?id=" + encodeURIComponent(connector.identifier);
			link.appendChild(img);
			link.appendChild(document.createTextNode(connector.name));
			link.appendChild(badge);
			cell1.appendChild(link);
			var button = document.createElement("button");
			button.type = "button";
			button.className = "btn btn-primary";
			button.appendChild(document.createTextNode("Pull from queue"));
			button.onclick = function () { onClickPull(connector); }
			cell2.appendChild(button);
			row.appendChild(cell1);
			row.appendChild(cell2);
			rows.appendChild(row);
		}
	}
}

function renderArtifacts() {
	var rows = document.getElementById("artifacts");
	for (var i = 0; i != ARTIFACTS.length; i++) {
		var group = ARTIFACTS[i];
		var topRow = document.createElement("tr");
		var cells = [
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td"),
			document.createElement("td")
		];
		cells[0].appendChild(document.createTextNode((i + 1).toString()));
		if (group.artifacts.length > 1) {
			var childRows = [];
			var button = document.createElement("span");
			button.setAttribute("aria-hidden", "true");
			button.className = "glyphicon glyphicon-chevron-down";
			button.style.cursor = "pointer";
			button.style.marginLeft = "10px";
			cells[0].appendChild(button);
			button.onclick = function () { onClickShowMore(group, button, rows, topRow, childRows) };
		}
		cells[1].appendChild(document.createTextNode(group.name));
		cells[2].appendChild(document.createTextNode(group.version));
		if (group.live.length == 0)
			cells[3].appendChild(document.createTextNode("none"));
		else if (group.live.length > 1)
			cells[3].appendChild(document.createTextNode("multiple"));
		else if (group.version === group.live[0].version)
			cells[3].appendChild(document.createTextNode("latest"));
		else
			cells[3].appendChild(document.createTextNode("older version"));
		for (var j = 0; j != cells.length; j++)
			topRow.appendChild(cells[j]);
		rows.appendChild(topRow);
	}
	document.getElementById("loader").style.display = "none";
}

function onClickShowMore(group, button, rows, parentRow, childRows) {
	if (button.className === "glyphicon glyphicon-chevron-down") {
		// down
		if (childRows.length === 0) {
			// render the children
			for (var i = 0; i != group.artifacts.length; i++) {
				var row = document.createElement("tr");
				var cells = [
					document.createElement("td"),
					document.createElement("td"),
					document.createElement("td"),
					document.createElement("td")
				];
				cells[1].appendChild(document.createTextNode(group.artifacts[i].name));
				cells[2].appendChild(document.createTextNode(group.artifacts[i].version));
				if (group.artifacts[i].isLive)
					cells[3].appendChild(document.createTextNode("yes"));
				for (var j = 0; j != cells.length; j++)
					row.appendChild(cells[j]);
				childRows.push(row);
			}
		}
		if (rows.lastChild === parentRow) {
			for (var j = 0; j != childRows.length; j++) {
				rows.appendChild(childRows[j]);
			}
		} else {
			var index = rows.childNodes.indexOf(parentRow);
			var target = rows.childNodes[index + 1];
			for (var j = 0; j != childRows.length; j++) {
				rows.insertBefore(childRows[j], target);
			}
		}
		button.className = "glyphicon glyphicon-chevron-up";
	} else {
		// up
		for (var i = 0; i != childRows.length; i++) {
			rows.removeChild(childRows[i]);
		}
		button.className = "glyphicon glyphicon-chevron-down";
	}
}

function onClickPull(connector) {
	xowl.pullFromConnector(function (status, ct, content) {
		if (status == 200) {
			document.location.href = "job.html?id=" + encodeURIComponent(content.identifier);
		} else {
			alert(content);
		}
	}, connector.identifier);
}
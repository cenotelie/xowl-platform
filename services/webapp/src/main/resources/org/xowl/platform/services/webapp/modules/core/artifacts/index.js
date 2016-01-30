// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTS_ALL = null;
var ARTIFACTS_LIVE = null;
var ARTIFACTS = null;
var CONNECTORS = null;
var JOB = null;
var DIFF_LEFT = null;
var DIFF_RIGHT = null;

function init() {
	xowl.getConnectors(function (status, ct, content) {
		if (status == 200) {
			CONNECTORS = content;
			renderConnectors();
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
	xowl.getAllArtifacts(function (status, ct, content) {
		if (status == 200) {
			ARTIFACTS_ALL = content;
			prepareArtifacts();
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
	xowl.getLiveArtifacts(function (status, ct, content) {
		if (status == 200) {
			ARTIFACTS_LIVE = content;
			prepareArtifacts();
		} else {
			displayMessage(getErrorFor(status, content));
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
	renderArtifactFamilies();
}

function renderConnectors() {
	var rows = document.getElementById("connectors");
	for (var i = 0; i != CONNECTORS.length; i++) {
		if (CONNECTORS[i].canPullInput) {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var img = document.createElement("img");
			img.src = "/web/assets/connector.svg";
			img.width = 32;
			img.height = 32;
			var link = document.createElement("a");
			link.href = "/web/modules/core/connectors/connector.html?id=" + encodeURIComponent(CONNECTORS[i].identifier);
			link.appendChild(img);
			link.appendChild(document.createTextNode(CONNECTORS[i].name));
			cell1.appendChild(link);
			var button = document.createElement("button");
			button.type = "button";
			button.className = "btn btn-primary";
			button.appendChild(document.createTextNode("Pull from client"));
			(function (connector) {
				button.onclick = function () { onClickPull(connector); }
			})(CONNECTORS[i]);
			cell2.appendChild(button);
			row.appendChild(cell1);
			row.appendChild(cell2);
			rows.appendChild(row);
		} else if (CONNECTORS[i].queue.length > 0) {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var img = document.createElement("img");
			img.src = "/web/assets/connector.svg";
			img.width = 32;
			img.height = 32;
			var badge = document.createElement("span");
			badge.className = "badge";
			badge.appendChild(document.createTextNode(CONNECTORS[i].queue.length.toString()));
			var link = document.createElement("a");
			link.href = "/web/modules/core/connectors/connector.html?id=" + encodeURIComponent(CONNECTORS[i].identifier);
			link.appendChild(img);
			link.appendChild(document.createTextNode(" "));
			link.appendChild(document.createTextNode(CONNECTORS[i].name));
			link.appendChild(document.createTextNode(" "));
			link.appendChild(badge);
			cell1.appendChild(link);
			var button = document.createElement("button");
			button.type = "button";
			button.className = "btn btn-primary";
			button.appendChild(document.createTextNode("Pull from queue"));
			(function (connector) {
				button.onclick = function () { onClickPull(connector); }
			})(CONNECTORS[i]);
			cell2.appendChild(button);
			row.appendChild(cell1);
			row.appendChild(cell2);
			rows.appendChild(row);
		}
	}
}

function renderArtifactFamilies() {
	var table = document.getElementById("artifacts");
	for (var i = 0; i != ARTIFACTS.length; i++) {
		var result = renderArtifactFamily(ARTIFACTS[i], i);
		table.appendChild(result.top);
		for (var j = 0; j != result.children.length; j++)
			table.appendChild(result.children[j]);
	}
	document.getElementById("loader").style.display = "none";
}

function renderArtifactFamily(family, index) {
	var topRow = document.createElement("tr");
	var cells = [
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")
	];
	var childRows = [];
	var button = document.createElement("span");
	button.setAttribute("aria-hidden", "true");
	button.className = "glyphicon glyphicon-chevron-down";
	button.style.cursor = "pointer";
	button.style.marginLeft = "10px";
	button.onclick = function () { onClickShowMore(button, childRows) };
	cells[0].appendChild(button);
	cells[0].appendChild(document.createTextNode((index + 1).toString()));
	cells[1].appendChild(document.createTextNode(family.name));
	cells[2].appendChild(document.createTextNode(family.version));
	if (family.live.length == 0)
		cells[3].appendChild(document.createTextNode("none"));
	else if (family.live.length > 1)
		cells[3].appendChild(document.createTextNode("multiple"));
	else if (family.version === family.live[0].version)
		cells[3].appendChild(document.createTextNode("latest"));
	else
		cells[3].appendChild(document.createTextNode("older version"));
	for (var i = 0; i != cells.length; i++)
		topRow.appendChild(cells[i]);
	for (var i = 0; i != family.artifacts.length; i++)
		childRows.push(renderArtifact(family.artifacts[i]));
	return { top: topRow, children: childRows };
}

function renderArtifact(artifact) {
	var row = document.createElement("tr");
	row.style.display = "none";
	var cells = [
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")
	];
	var diff = document.createElement("span");
	diff.className = "badge";
	diff.style.cursor = "pointer";
	diff.appendChild(document.createTextNode("diff"));
	diff.onclick = function () { onClickSelectDiff(artifact); }
	cells[0].appendChild(diff);
	var link = document.createElement("a");
	link.href = "artifact.html?id=" + encodeURIComponent(artifact.identifier);
	link.appendChild(document.createTextNode(artifact.name));
	cells[1].appendChild(link);
	cells[2].appendChild(document.createTextNode(artifact.version));
	var toggle = document.createElement("div");
	toggle.className = "toggle-button" + (artifact.isLive ? " toggle-button-selected" : "");
	toggle.appendChild(document.createElement("button"));
	toggle.onclick = function () { onClickToggleLive(artifact); }
	cells[3].appendChild(toggle);
	for (var j = 0; j != cells.length; j++)
		row.appendChild(cells[j]);
	return row;
}

function onClickShowMore(button, childRows) {
	if (button.className === "glyphicon glyphicon-chevron-down") {
		// down
		for (var i = 0; i != childRows.length; i++)
			childRows[i].style.display = "";
		button.className = "glyphicon glyphicon-chevron-up";
	} else {
		// up
		for (var i = 0; i != childRows.length; i++)
			childRows[i].style.display = "none";
		button.className = "glyphicon glyphicon-chevron-down";
	}
}

function onClickPull(connector) {
	if (JOB !== null) {
		alert("Please wait for the previous action to terminate.");
		return;
	}
	JOB = "reserved";
	xowl.pullFromConnector(function (status, ct, content) {
		if (status == 200) {
			trackJob(content.identifier, "Working ...", function (isSuccess) {
				if (isSuccess)
					window.location.reload(true);
			});
		} else {
			displayMessage(getErrorFor(status, content));
			JOB = null;
		}
	}, connector.identifier);
}

function onClickToggleLive(artifact) {
	if (JOB !== null) {
		alert("Please wait for the previous action to terminate.");
		return;
	}
	JOB = "reserved";
	var callback = function (status, ct, content) {
		if (status == 200) {
			trackJob(content.identifier, "Working ...", function (isSuccess) {
				if (isSuccess)
					window.location.reload(true);
			});
		} else {
			displayMessage(getErrorFor(status, content));
			JOB = null;
		}
	};
	if (artifact.isLive)
		xowl.pullFromLive(callback, artifact.identifier);
	else
		xowl.pushToLive(callback, artifact.identifier);
}

function onClickSelectDiff(artifact) {
	if (DIFF_LEFT == null) {
		if (DIFF_RIGHT !== null && DIFF_RIGHT.base !== artifact.base)
			return;
		DIFF_LEFT = artifact;
		document.getElementById("diff-left").value = artifact.name + " (" + artifact.version + ")";
		if (DIFF_RIGHT !== null)
			document.getElementById("diff-target").href = "diff.html?left=" + encodeURIComponent(DIFF_LEFT.identifier) + "&right=" + encodeURIComponent(DIFF_RIGHT.identifier);
	} else {
		if (DIFF_LEFT !== null && DIFF_LEFT.base !== artifact.base)
			return;
		DIFF_RIGHT = artifact;
		document.getElementById("diff-right").value = artifact.name + " (" + artifact.version + ")";
		if (DIFF_LEFT !== null)
			document.getElementById("diff-target").href = "diff.html?left=" + encodeURIComponent(DIFF_LEFT.identifier) + "&right=" + encodeURIComponent(DIFF_RIGHT.identifier);
	}
}

function onClickDiffClearLeft() {
	DIFF_LEFT = null;
	document.getElementById("diff-left").value = "";
	document.getElementById("diff-target").href = null;
}

function onClickDiffClearRight() {
	DIFF_RIGHT = null;
	document.getElementById("diff-right").value = "";
	document.getElementById("diff-target").href = null;
}

function onClickDiffInverse() {
	var temp = DIFF_LEFT;
	DIFF_LEFT = DIFF_RIGHT;
	DIFF_RIGHT = temp;
	var displayLeft = document.getElementById("diff-left");
	var displayRight = document.getElementById("diff-right");
	temp = displayLeft.value;
	displayLeft.value = displayRight.value;
	displayRight.value = temp;
	if (DIFF_LEFT !== null && DIFF_RIGHT !== null)
		document.getElementById("diff-target").href = "diff.html?left=" + encodeURIComponent(DIFF_LEFT.identifier) + "&right=" + encodeURIComponent(DIFF_RIGHT.identifier);
}
// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTS_ALL = null;
var ARTIFACTS_LIVE = null;
var ARTIFACTS = null;
var CONNECTORS = null;
var DIFF_LEFT = null;
var DIFF_RIGHT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Artifacts Management"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 3))
		return;
	xowl.getConnectors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			CONNECTORS = content;
			renderConnectors();
		}
	});
	xowl.getAllArtifacts(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			ARTIFACTS_ALL = content;
			if (ARTIFACTS_LIVE !== null)
				prepareArtifacts();
		}
	});
	xowl.getLiveArtifacts(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			ARTIFACTS_LIVE = content;
			if (ARTIFACTS_ALL !== null)
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
		ARTIFACTS[i].artifacts.sort(compareArtifacts);
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
			img.title = CONNECTORS[i].identifier;
			var link = document.createElement("a");
			link.href = "/web/modules/admin/connectors/connector.html?id=" + encodeURIComponent(CONNECTORS[i].identifier);
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
			img.title = CONNECTORS[i].identifier;
			var badge = document.createElement("span");
			badge.className = "badge";
			badge.appendChild(document.createTextNode(CONNECTORS[i].queue.length.toString()));
			var link = document.createElement("a");
			link.href = "/web/modules/admin/connectors/connector.html?id=" + encodeURIComponent(CONNECTORS[i].identifier);
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
	var icon = document.createElement("img");
	icon.src = "/web/assets/artifact_family.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = family.name;
	cells[1].appendChild(icon);
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
	var icon = document.createElement("img");
	icon.src = "/web/assets/artifact.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = artifact.identifier;
	cells[1].appendChild(icon);
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
	var result = confirm("Pull from connector " + connector.name + "?");
	if (!result)
		return;
	if (!onOperationRequest("Launching a pull artifact operation from " + connector.name + " ..."))
		return;
	xowl.pullFromConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched job ", content]});
			waitForJob(content.identifier, content.name, function (job) {
				onPullJobComplete(job.result);
			});
		}
	}, connector.identifier);
}

function onPullJobComplete(xsp) {
	if (!xsp.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!xsp.isSuccess) {
		displayMessage("error", "FAILURE: " + xsp.message);
	} else {
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: [
			"Pulled artifact ",
			{type: "org.xowl.platform.kernel.artifacts.Artifact", identifier: xsp.payload, name: xsp.payload}]});
		// TODO: do not do a full reload
		waitAndRefresh();
	}
}

function onClickToggleLive(artifact) {
	if (artifact.isLive) {
		doPullFromLive(artifact);
	} else {
		doPushToLive(artifact);
	}
}

function doPushToLive(artifact) {
	var result = confirm("Activate " + artifact.name + " for live reasoning?");
	if (!result)
		return;
	if (!onOperationRequest("Launching an activation job for artifact " + artifact.name + " ..."))
		return;
	xowl.pushArtifactToLive(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched job ", content]});
			waitForJob(content.identifier, content.name, function (job) {
				onPushToLiveCompleted(job.result, artifact);
			});
		}
	}, artifact.identifier);
}

function onPushToLiveCompleted(xsp, artifact) {
	if (!xsp.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!xsp.isSuccess) {
		displayMessage("error", "FAILURE: " + xsp.message);
	} else {
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: [
			"Activated artifact ",
			artifact,
			" for live reasoning."]});
		// TODO: do not do a full reload
		waitAndRefresh();
	}
}

function doPullFromLive(artifact) {
	var result = confirm("De-activate " + artifact.name + " for live reasoning?");
	if (!result)
		return;
	if (!onOperationRequest("Launching an de-activation job for artifact " + artifact.name + " ..."))
		return;
	xowl.pullArtifactFromLive(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched job ", content]});
			waitForJob(content.identifier, content.name, function (job) {
				onPullFromLiveCompleted(job.result, artifact);
			});
		}
	}, artifact.identifier);
}

function onPullFromLiveCompleted(xsp, artifact) {
	if (!xsp.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!xsp.isSuccess) {
		displayMessage("error", "FAILURE: " + xsp.message);
	} else {
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: [
			"De-activated artifact ",
			artifact,
			" for live reasoning."]});
		// TODO: do not do a full reload
		waitAndRefresh();
	}
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
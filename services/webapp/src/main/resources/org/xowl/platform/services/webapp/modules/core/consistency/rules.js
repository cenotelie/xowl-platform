// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "Rules"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getConsistencyRules(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderRules(content);
			}
		});
	});
}

function renderRules(rules) {
	rules.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("rules");
	for (var i = 0; i != rules.length; i++) {
		table.appendChild(renderRule(rules[i], i));
	}
	document.getElementById("btn-download").href = "data:" + MIME_JSON + ";charset=utf-8;base64," + btoa(JSON.stringify(rules));
}

function renderRule(rule, index) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	cell.appendChild(document.createTextNode((index + 1).toString()));
	row.appendChild(cell);

	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/rule.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = rule.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(rule.name));
	link.href = "rule.html?id=" + encodeURIComponent(rule.identifier);
	cell = document.createElement("td");
	cell.appendChild(icon);
	cell.appendChild(link);
	row.appendChild(cell);

	var toggle = document.createElement("div");
	toggle.appendChild(document.createElement("button"));
	toggle.classList.add("toggle-button");
	if (rule.isActive)
		toggle.classList.add("toggle-button-selected");
	toggle.onclick = function (evt) { onToggleRule(toggle, rule); };
	cell = document.createElement("td");
	cell.appendChild(toggle);
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(image);
	button.onclick = function (evt) { onDeleteRule(rule); };
	cell.appendChild(button);
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-download.svg";
	image.width = 20;
	image.height = 20;
	image.title = "DOWNLOAD";
	button = document.createElement("a");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.href = "data:" + MIME_JSON + ";charset=utf-8;base64," + btoa(JSON.stringify(rule));
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onToggleRule(toggle, rule) {
	if (rule.isActive) {
		doDeactivateRule(toggle, rule);
	} else {
		doActivateRule(toggle, rule);
	}
}

function doActivateRule(toggle, rule) {
	var result = confirm("Activate consistency rule " + rule.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Activating rule ", rule, " ..."]}))
		return;
	xowl.activateConsistencyRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Activated rule ", rule, "."]});
			rule.isActive = true;
			toggle.classList.add("toggle-button-selected");
		}
	}, rule.identifier);
}

function doDeactivateRule(toggle, rule) {
	var result = confirm("De-activate consistency rule " + rule.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["De-activating rule ", rule, " ..."]}))
		return;
	xowl.deactivateConsistencyRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["De-activated rule ", rule, "."]});
			rule.isActive = false;
			toggle.classList.remove("toggle-button-selected");
		}
	}, rule.identifier);
}

function onDeleteRule(rule) {
	var result = confirm("Delete rule " + rule.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Deleting rule ", rule, " ..."]}))
		return;
	xowl.deleteConsistencyRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Deleted rule ", rule, "."]});
			// TODO: do not do a full reload
			waitAndRefresh();
		}
	}, rule.identifier);
}
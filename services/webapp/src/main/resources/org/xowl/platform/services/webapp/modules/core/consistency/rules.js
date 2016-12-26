// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Consistency Management", uri: "/web/modules/core/consistency/"},
			{name: "Consistency Rules"}], function() {
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
	var table = document.getElementById("rules");
	for (var i = 0; i != rules.length; i++) {
		table.appendChild(renderRule(rules[i], i));
	}
}

function renderRule(rule, index) {
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode((index + 1).toString()));
	var icon = document.createElement("img");
	icon.src = "/web/assets/rule.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(rule.name));
	link.href = "rule.html?id=" + encodeURIComponent(rule.identifier);
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(link);
	var toggle = document.createElement("div");
	toggle.appendChild(document.createElement("button"));
	toggle.classList.add("toggle-button");
	if (rule.isActive)
		toggle.classList.add("toggle-button-selected");
	toggle.onclick = function (evt) { onToggleRule(toggle, rule); };
	var cell3 = document.createElement("td");
	cell3.appendChild(toggle);
	var span = document.createElement("span");
	span.classList.add("glyphicon");
	span.classList.add("glyphicon-minus");
	span.setAttribute("aria-hidden", "true");
	var button = document.createElement("a");
	button.classList.add("btn");
	button.classList.add("btn-xs");
	button.classList.add("btn-danger");
	button.title = "DELETE";
	button.appendChild(span);
	button.onclick = function (evt) { onDeleteRule(rule); };
	var cell4 = document.createElement("td");
	cell4.appendChild(button);
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
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
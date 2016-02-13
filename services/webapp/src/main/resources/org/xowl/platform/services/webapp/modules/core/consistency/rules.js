// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var FLAG = false;

function init() {
	setupPage(xowl);
	xowl.getConsistencyRules(function (status, ct, content) {
		if (status == 200) {
			renderRules(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
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
	link.href = "rule.html?id=" + encodeURIComponent(rule.id);
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(link);
	var toggle = document.createElement("div");
	toggle.appendChild(document.createElement("button"));
	toggle.classList.add("toggle-button");
	if (rule.isActive)
		toggle.classList.add("toggle-button-selected");
	toggle.onclick = function (evt) { onToggleRule(rule); };
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

function onToggleRule(rule) {
	if (FLAG)
		return;
	FLAG = true;
	if (rule.isActive) {
		displayMessage("Deactivating the rule ...");
		xowl.deactivateConsistencyRule(function (status, ct, content) {
			if (status == 200) {
				window.location.reload();
			} else {
				displayMessage(getErrorFor(status, content));
			}
		}, rule.id);
	} else {
		displayMessage("Activating the rule ...");
		xowl.activateConsistencyRule(function (status, ct, content) {
			if (status == 200) {
				window.location.reload();
			} else {
				displayMessage(getErrorFor(status, content));
			}
		}, rule.id);
	}
}

function onDeleteRule(rule) {
	if (FLAG)
		return;
	FLAG = true;
	displayMessage("Deleting the rule ...");
	xowl.deleteConsistencyRule(function (status, ct, content) {
		if (status == 200) {
			window.location.reload();
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, rule.id);
}
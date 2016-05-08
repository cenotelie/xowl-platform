// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	xowl.getInconsistencies(function (status, ct, content) {
		if (status == 200) {
			renderInconsistencies(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderInconsistencies(inconsistencies) {
	var table = document.getElementById("inconsistencies");
	for (var i = 0; i != inconsistencies.length; i++) {
		table.appendChild(renderInconsistency(inconsistencies[i], i));
	}
}

function renderInconsistency(inconsistency, index) {
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode((index + 1).toString()));
	var icon = document.createElement("img");
	icon.src = "/web/assets/inconsistency.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(renderMessage(inconsistency));
	icon = document.createElement("img");
	icon.src = "/web/assets/rule.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(inconsistency.ruleName));
	link.href = "rule.html?id=" + encodeURIComponent(inconsistency.ruleId);
	var cell3 = document.createElement("td");
	cell3.appendChild(icon);
	cell3.appendChild(link);
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	return row;
}

function renderMessage(inconsistency) {
	var span = document.createElement("span");
	if (inconsistency.message.indexOf("?") < 0) {
		span.appendChild(document.createTextNode(inconsistency.message));
		return span;
	}
	var buffer = "";
	for (var i = 0; i < inconsistency.message.length; i++) {
		var c = inconsistency.message.charAt(i);
		if (c === '?') {
			var j = i + 1;
			while (j < inconsistency.message.length && inconsistency.message.charAt(j).match(/[a-zA-Z0-9]/i))
				j++;
			if (j == i + 1)
				continue;
			var name = inconsistency.message.substring(i + 1, j);
			if (!inconsistency.antecedents.hasOwnProperty(name))
				continue;
			span.appendChild(document.createTextNode(buffer));
			span.appendChild(rdfToDom(inconsistency.antecedents[name]));
			buffer = "";
			i = j - 1;
		} else {
			buffer = buffer + c.toString();
		}
	}
	if (buffer.length > 0)
		span.appendChild(document.createTextNode(buffer));
	return span;
}
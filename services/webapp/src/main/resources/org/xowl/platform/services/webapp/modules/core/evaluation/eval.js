// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var evalId = getParameterByName("id");

function init() {
	setupPage(xowl);
	if (!evalId || evalId === null || evalId === "")
		return;
	document.getElementById("placeholder-eval").innerHTML = evalId;
	displayMessage("Loading ...");
	xowl.getEvaluation(function (status, ct, content) {
		if (status == 200) {
			renderEval(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, evalId);
}

function renderEval(evaluation) {
	document.getElementById("input-eval-name").value = evaluation.name;
	var heads = document.getElementById("table-heads");
	for (var i = 0; i != evaluation.evaluables.length; i++) {
		var icon = document.createElement("img");
		icon.src = "/web/assets/element.svg";
		icon.width = 40;
		icon.height = 40;
		icon.style.marginRight = "20px";
		var head = document.createElement("td");
		head.appendChild(icon);
		head.appendChild(document.createTextNode(evaluation.evaluables[i].name));
		heads.appendChild(head);
	}
	var content = document.getElementById("table-content");
	for (var i = 0; i != evaluation.criteria.length; i++) {
		var row = document.createElement("tr");
		var icon = document.createElement("img");
		icon.src = "/web/assets/criterion.svg";
		icon.width = 40;
		icon.height = 40;
		icon.style.marginRight = "20px";
		var cell = document.createElement("td");
		cell.appendChild(icon);
		cell.appendChild(document.createTextNode(evaluation.criteria[i].name));
		row.appendChild(cell);
		for (var j = 0; j != evaluation.evaluables.length; j++) {
			cell = document.createElement("td");
			var result = getResult(evaluation, evaluation.criteria[i], evaluation.evaluables[j]);
			if (result != null)
				cell.appendChild(document.createTextNode(result));
			row.appendChild(cell);
		}
		content.appendChild(row);
	}
	displayMessage(null);
}

function getResult(evaluation, criterion, evaluable) {
	for (var i = 0; i != evaluation.results.length; i++) {
		if (evaluation.results[i].evaluable === evaluable.identifier && evaluation.results[i].criterion === criterion.identifier)
			return evaluation.results[i].data;
	}
	return null;
}
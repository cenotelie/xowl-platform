// Copyright (c) 2016 Laurent Wouters
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
		var head = document.createElement("td");
		head.appendChild(document.createTextNode(evaluation.evaluables[i].name));
		heads.appendChild(head);
	}
	var content = document.getElementById("table-content");
	for (var i = 0; i != evaluation.criteria.length; i++) {
		var row = document.createElement("tr");
		var cell = document.createElement("td");
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
		if (evaluation.results[i].evaluable === evaluable.id && evaluation.results[i].criterion === criterion.id)
			return evaluation.results[i].data;
	}
	return null;
}
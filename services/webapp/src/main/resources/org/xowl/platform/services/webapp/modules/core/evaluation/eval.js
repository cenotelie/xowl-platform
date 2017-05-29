// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var evalId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Analysis: Evaluation", uri: ROOT + "/modules/core/evaluation/"},
			{name: "Evaluation " + evalId}], function() {
		if (!evalId || evalId === null || evalId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getEvaluation(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderEval(content);
			}
		}, evalId);
	});
}

function renderEval(evaluation) {
	document.getElementById("input-eval-name").value = evaluation.name;
	var heads = document.getElementById("table-heads");
	for (var i = 0; i != evaluation.evaluables.length; i++) {
		var icon = document.createElement("img");
		icon.src = ROOT + "/assets/element.svg";
		icon.width = 40;
		icon.height = 40;
		icon.style.marginRight = "20px";
		icon.title = evaluation.evaluables[i].identifier;
		var head = document.createElement("td");
		head.appendChild(icon);
		head.appendChild(document.createTextNode(evaluation.evaluables[i].name));
		heads.appendChild(head);
	}
	var content = document.getElementById("table-content");
	for (var i = 0; i != evaluation.criteria.length; i++) {
		var row = document.createElement("tr");
		var icon = document.createElement("img");
		icon.src = ROOT + "/assets/criterion.svg";
		icon.width = 40;
		icon.height = 40;
		icon.style.marginRight = "20px";
		icon.title = evaluation.criteria[i].identifier;
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
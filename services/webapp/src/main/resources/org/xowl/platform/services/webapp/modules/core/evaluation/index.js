// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Evaluation Analysis"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getEvaluations(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderEvaluations(content);
			}
		});
	});
}

function renderEvaluations(evaluations) {
	var table = document.getElementById("evaluations");
	for (var i = 0; i != evaluations.length; i++) {
		var row = renderEvaluation(i, evaluations[i]);
		table.appendChild(row);
	}
}

function renderEvaluation(index, evaluation) {
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode((index + 1).toString()));
	var icon = document.createElement("img");
	icon.src = "/web/assets/evaluation.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var link = document.createElement("a");
	link.href = "eval.html?id=" + encodeURIComponent(evaluation.identifier);
	link.appendChild(document.createTextNode(evaluation.name));
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(link);
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	return row;
}
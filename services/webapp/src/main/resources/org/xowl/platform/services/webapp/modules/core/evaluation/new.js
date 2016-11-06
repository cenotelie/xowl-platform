// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();
var EVALUABLES = null;
var CRITERIA_TYPES = [];
var CRITERIA_SELECTED = [];

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Evaluation Analysis", uri: "/web/modules/core/evaluation/"},
			{name: "New Evaluation"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getEvaluableTypes(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderEvaluableTypes(content);
			}
		});
	});
}

function renderEvaluableTypes(types) {
	types.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("input-evaluable-type");
	for (var i = 0; i != types.length; i++) {
		var option = document.createElement("option");
		option.value = types[i].identifier;
		option.appendChild(document.createTextNode(types[i].name));
		select.appendChild(option);
	}
	if (types.length > 0) {
		select.value = types[0].identifier;
		onEvaluableTypeChanged();
	}
}

function onEvaluableTypeChanged() {
	var typeId = document.getElementById("input-evaluable-type").value;
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getEvaluables(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderEvaluables(content);
			doGetCriteria(typeId);
		}
	}, typeId);
}

function doGetCriteria(typeId) {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getEvaluationCriteria(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderCriteriaTypes(content);
		}
	}, typeId);
}

function renderEvaluables(data) {
	EVALUABLES = data;
	data.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("evaluables");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	for (var i = 0; i != data.length; i++) {
		var row = renderEvaluable(data[i]);
		table.appendChild(row);
	}
}

function renderEvaluable(evaluable) {
	evaluable.selected = false;
	var icon = document.createElement("img");
	icon.src = "/web/assets/element.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var cell1 = document.createElement("td");
	cell1.appendChild(icon);
	cell1.appendChild(document.createTextNode(evaluable.name));
	var toggle = document.createElement("div");
	toggle.appendChild(document.createElement("button"));
	toggle.classList.add("toggle-button");
	toggle.onclick = function (evt) {
		evaluable.selected = !evaluable.selected;
		if (evaluable.selected)
			toggle.className = "toggle-button toggle-button-selected";
		else
			toggle.className = "toggle-button";
	};
	var cell2 = document.createElement("td");
	cell2.appendChild(toggle);
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	return row;
}

function renderCriteriaTypes(criteria) {
	criteria.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	CRITERIA_TYPES = criteria;
	CRITERIA_SELECTED = [];
	var table = document.getElementById("criteria");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	var select = document.getElementById("input-criterion");
	while (select.hasChildNodes())
		select.removeChild(select.lastChild);
	for (var i = 0; i != criteria.length; i++) {
		var option = document.createElement("option");
		option.value = criteria[i].identifier;
		option.appendChild(document.createTextNode(criteria[i].name));
		select.appendChild(option);
	}
	if (criteria.length > 0) {
		select.value = criteria[0].identifier;
		onCriterionChanged();
	}
}

function onCriterionChanged() {

}

function onCriterionAdd() {
	var criterionTypeId = document.getElementById("input-criterion").value;
	var criterionType = null;
	for (var i = 0; i != CRITERIA_TYPES.length; i++) {
		if (CRITERIA_TYPES[i].identifier === criterionTypeId) {
			criterionType = CRITERIA_TYPES[i];
			break;
		}
	}
	var criterion = {
		typeId: criterionTypeId,
		name: criterionType.name,
		parameters: {}
	};
	CRITERIA_SELECTED.push(criterion);
	var table = document.getElementById("criteria");
	table.appendChild(renderCriterion(criterion));
}

function renderCriterion(criterion) {
	var row = document.createElement("tr");
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
	button.onclick = function (evt) {
		document.getElementById("criteria").removeChild(row);
		CRITERIA_SELECTED.splice(CRITERIA_SELECTED.indexOf(criterion), 1);
	};
	var cell1 = document.createElement("td");
	cell1.appendChild(button);
	var icon = document.createElement("img");
	icon.src = "/web/assets/criterion.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(document.createTextNode(criterion.name));
	row.appendChild(cell1);
	row.appendChild(cell2);
	return row;
}

function onEvaluationGo() {
	if (!onOperationRequest("Creating evaluation ..."))
		return;
	var name = document.getElementById("input-eval-name").value;
	if (name === null || name === "") {
		onOperationAbort("Expected a name for the evaluation!");
		return;
	}
	var definition = {
		name: name,
		evaluableType: document.getElementById("input-evaluable-type").value,
		evaluables: [],
		criteria: CRITERIA_SELECTED
	};
	for (var i = 0; i != EVALUABLES.length; i++) {
		if (EVALUABLES[i].selected)
			definition.evaluables.push(EVALUABLES[i]);
	}
	xowl.newEvaluation(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.platform.kernel.RichString", parts: ["Created evaluation ", content, "."]});
			waitAndGo("eval.html?id=" + encodeURIComponent(content.identifier));
		}
	}, definition);
}
// Copyright (c) 2015 Laurent Wouters
// Provided under LGPL v3

var xowl = new XOWL();
var EVALUABLES = null;
var CRITERIA_TYPES = [];
var CRITERIA_SELECTED = [];
var BUSY = false;

function init() {
    setupPage(xowl);
    xowl.getEvaluableTypes(function (status, ct, content) {
        if (status == 200) {
            renderEvaluableTypes(content);
        } else {
            displayMessage(getErrorFor(status, content));
        }
    });
}

function renderEvaluableTypes(types) {
    types.sort(function (x, y) {
        return x.name.localeCompare(y.name);
    });
    var select = document.getElementById("input-evaluable-type");
    for (var i = 0; i != types.length; i++) {
        var option = document.createElement("option");
        option.value = types[i].id;
        option.appendChild(document.createTextNode(types[i].name));
        select.appendChild(option);
    }
    if (types.length > 0) {
        select.value = types[0].id;
        onEvaluableTypeChanged();
    } else {
        displayMessage(null);
    }
}

function onEvaluableTypeChanged() {
    displayMessage("Loading ...");
    var typeId = document.getElementById("input-evaluable-type").value;
    xowl.getEvaluables(function (status, ct, content) {
        if (status == 200) {
            renderEvaluables(content);
            xowl.getEvaluationCriteria(function (status, ct, content) {
                if (status == 200) {
                    renderCriteriaTypes(content);
                    displayMessage(null);
                } else {
                    displayMessage(getErrorFor(status, content));
                }
            }, typeId);
        } else {
            displayMessage(getErrorFor(status, content));
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
        option.value = criteria[i].id;
        option.appendChild(document.createTextNode(criteria[i].name));
        select.appendChild(option);
    }
    if (criteria.length > 0) {
        select.value = criteria[0].id;
        onCriterionChanged();
    }
}

function onCriterionChanged() {

}

function onCriterionAdd() {
    var criterionTypeId = document.getElementById("input-criterion").value;
    var criterionType = null;
    for (var i = 0; i != CRITERIA_TYPES.length; i++) {
        if (CRITERIA_TYPES[i].id === criterionTypeId) {
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
	icon.src = "/web/assets/evaluation.svg";
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
    if (BUSY)
        return;
    var name = document.getElementById("input-eval-name").value;
    if (name === null || name === "")
        return;
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
    BUSY = true;
    xowl.newEvaluation(function (status, ct, content) {
        if (status == 200) {
            window.location.href = "eval.html?id=" + encodeURIComponent(content.id);
        } else {
            displayMessage(getErrorFor(status, content));
        }
    }, definition);
}
// Copyright (c) 2015 Laurent Wouters
// Provided under LGPL v3

var xowl = new XOWL();

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
                    renderCriteria(content);
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
    data.sort(function (x, y) {
        return x.name.localeCompare(y.name);
    });
    var table = document.getElementById("evaluables");
    for (var i = 0; i != data.length; i++) {
        var row = renderEvaluable(data[i]);
        table.appendChild(row);
    }
}

function renderEvaluable(evaluable) {
    evaluable.selected = false;
    var cell1 = document.createElement("td");
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

function renderCriteria(criteria) {
    criteria.sort(function (x, y) {
        return x.name.localeCompare(y.name);
    });
    var select = document.getElementById("input-evaluable-type");
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

}

function onEvaluationGo() {
}